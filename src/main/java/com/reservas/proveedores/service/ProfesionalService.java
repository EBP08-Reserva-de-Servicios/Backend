package com.reservas.proveedores.service;

import com.reservas.proveedores.api.ConsultaProfesionales;
import com.reservas.proveedores.api.ProfesionalDetalle;
import com.reservas.proveedores.api.ProfesionalResumen;
import com.reservas.proveedores.api.VerificadorOfertas;
import com.reservas.proveedores.model.Profesional;
import com.reservas.proveedores.repository.ProfesionalRepository;
import com.reservas.proveedores.repository.ProveedorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HU-29: registro y consulta de profesionales de un proveedor. HU-20: edición con las mismas reglas.
 * Mismo patrón de A1/O1: obligatorios y formatos (400) -> duplicados (409) -> guardar.
 * También implementa ConsultaProfesionales (api) para que agendamiento muestre el profesional de cada bloque.
 */
@Service
public class ProfesionalService implements ConsultaProfesionales {

    // Regla de la pantalla del profesional en el prototipo
    private static final String REGEX_CORREO_PROFESIONAL = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$";
    private static final Pattern DATA_URL_IMAGEN =
            Pattern.compile("^data:image/(png|jpe?g);base64,(.+)$", Pattern.DOTALL);
    private static final int MAX_BYTES_FOTO = 5 * 1024 * 1024;   // 5 MB

    private final ProfesionalRepository profesionalRepository;
    private final ProveedorRepository proveedorRepository;
    private final VerificadorOfertas verificadorOfertas;          // lo implementa catalogo
    private final HorarioService horarioService;                  // HU-20: regenerar bloques si cambian los servicios

    /** Datos ya validados: los servicios sin repetidos y la foto decodificada (null si no se envió). */
    private record DatosValidados(List<Long> servicioProveedorIds, byte[] foto, String fotoTipo) {
    }

    public ProfesionalService(ProfesionalRepository profesionalRepository,
                              ProveedorRepository proveedorRepository,
                              VerificadorOfertas verificadorOfertas,
                              HorarioService horarioService) {
        this.profesionalRepository = profesionalRepository;
        this.proveedorRepository = proveedorRepository;
        this.verificadorOfertas = verificadorOfertas;
        this.horarioService = horarioService;
    }

    public ProfesionalDetalle registrar(Long nit, Profesional p, String fotoDataUrl, List<Long> servicioProveedorIds) {
        verificarProveedor(nit);
        normalizar(p);

        // 1) Obligatorios y formatos (400)   2) Duplicados (409)
        DatosValidados datos = validar(nit, p, fotoDataUrl, servicioProveedorIds);
        verificarDuplicados(nit, p, null);

        // 3) Guardar
        p.setProveedorNit(nit);
        p.setFoto(datos.foto());
        p.setFotoTipo(datos.fotoTipo());
        p.setServicioProveedorIds(new ArrayList<>(datos.servicioProveedorIds()));
        return aDetalle(profesionalRepository.save(p));
    }

    /**
     * HU-20: edita un profesional del proveedor con el mismo cuerpo del registro.
     * foto = null (o vacía) conserva la foto actual. Los duplicados se comparan con los DEMÁS profesionales.
     * Si cambian los servicios que presta, se regeneran sus bloques libres (las citas se conservan),
     * todo en la misma transacción: si la regeneración falla, la edición tampoco se guarda.
     */
    @Transactional
    public ProfesionalDetalle editar(Long nit, String id, Profesional cambios, String fotoDataUrl,
                                     List<Long> servicioProveedorIds) {
        verificarProveedor(nit);
        Profesional actual = profesionalRepository.findByIdAndProveedorNit(id, nit)
                .orElseThrow(() -> new RegistroProfesionalException(RegistroProfesionalException.Tipo.NO_ENCONTRADO,
                        List.of("Profesional no encontrado.")));
        normalizar(cambios);

        DatosValidados datos = validar(nit, cambios, fotoDataUrl, servicioProveedorIds);
        verificarDuplicados(nit, cambios, id);

        boolean cambiaronServicios = !new HashSet<>(actual.getServicioProveedorIds())
                .equals(new HashSet<>(datos.servicioProveedorIds()));

        actual.setTipoDocumento(cambios.getTipoDocumento());
        actual.setNumeroDocumento(cambios.getNumeroDocumento());
        actual.setNombres(cambios.getNombres());
        actual.setApellidos(cambios.getApellidos());
        actual.setCorreo(cambios.getCorreo());
        actual.setTelefono(cambios.getTelefono());
        if (datos.foto() != null) {                                    // sin foto nueva: se conserva la actual
            actual.setFoto(datos.foto());
            actual.setFotoTipo(datos.fotoTipo());
        }
        actual.setServicioProveedorIds(new ArrayList<>(datos.servicioProveedorIds()));
        Profesional guardado = profesionalRepository.save(actual);

        if (cambiaronServicios) {
            horarioService.alCambiarServiciosDelProfesional(guardado);
        }
        return aDetalle(guardado);
    }

    public List<ProfesionalDetalle> listar(Long nit) {
        verificarProveedor(nit);
        return profesionalRepository.findByProveedorNitOrderByNombresAscApellidosAsc(nit).stream()
                .map(this::aDetalle).toList();
    }

    // ---- Validaciones compartidas por registrar (HU-29) y editar (HU-20) ----

    private static void normalizar(Profesional p) {
        p.setNombres(limpiar(p.getNombres()));
        p.setApellidos(limpiar(p.getApellidos()));
        p.setTipoDocumento(vacio(p.getTipoDocumento()) ? null : p.getTipoDocumento().trim());
        p.setNumeroDocumento(limpiar(p.getNumeroDocumento()));
        p.setCorreo(limpiar(p.getCorreo()));
        p.setTelefono(normalizarTelefono(p.getTelefono()));
    }

    /** Obligatorios y formatos, todos juntos (400). Devuelve los servicios sin repetidos y la foto decodificada. */
    private DatosValidados validar(Long nit, Profesional p, String fotoDataUrl, List<Long> servicioProveedorIds) {
        List<String> errores = new ArrayList<>();
        if (vacio(p.getNombres()))         errores.add("El nombre es obligatorio.");
        if (vacio(p.getApellidos()))       errores.add("El apellido es obligatorio.");
        if (vacio(p.getNumeroDocumento())) errores.add("El número de documento es obligatorio.");
        if (vacio(p.getCorreo())) {
            errores.add("El correo es obligatorio.");
        } else if (!p.getCorreo().matches(REGEX_CORREO_PROFESIONAL)) {
            errores.add("El correo electrónico no es válido.");
        }
        if (vacio(p.getTelefono())) {
            errores.add("El teléfono es obligatorio.");
        } else if (!p.getTelefono().matches("^\\d{10}$")) {
            errores.add("El teléfono debe tener 10 dígitos numéricos.");   // igual que el prototipo (observación de Arelis)
        }

        List<Long> ids = (servicioProveedorIds == null)
                ? List.of()
                : servicioProveedorIds.stream().filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) {
            errores.add("Debes seleccionar al menos un servicio");
        } else if (!verificadorOfertas.pertenecenAlProveedor(nit, ids)) {
            errores.add("Alguno de los servicios seleccionados no pertenece a este proveedor.");
        }

        byte[] foto = null;
        String fotoTipo = null;
        if (!vacio(fotoDataUrl)) {
            Matcher m = DATA_URL_IMAGEN.matcher(fotoDataUrl.trim());
            if (!m.matches()) {
                errores.add("Sube una imagen en formato PNG o JPG");
            } else {
                try {
                    foto = Base64.getDecoder().decode(m.group(2).replaceAll("\\s", ""));
                    fotoTipo = m.group(1).equals("png") ? "image/png" : "image/jpeg";
                    if (foto.length > MAX_BYTES_FOTO) {
                        errores.add("La imagen no puede superar 5 MB.");
                    }
                } catch (IllegalArgumentException e) {
                    errores.add("Sube una imagen en formato PNG o JPG");
                }
            }
        }

        if (!errores.isEmpty()) {
            throw new RegistroProfesionalException(RegistroProfesionalException.Tipo.INVALIDO, errores);
        }
        return new DatosValidados(ids, foto, fotoTipo);
    }

    /**
     * Duplicados (409, textos exactos del prototipo, sin punto final).
     * excluirId = null al registrar; al editar, el id del profesional (no se compara consigo mismo).
     */
    private void verificarDuplicados(Long nit, Profesional p, String excluirId) {
        boolean editando = excluirId != null;
        List<String> duplicados = new ArrayList<>();
        if (editando ? profesionalRepository.existsByNumeroDocumentoAndIdNot(p.getNumeroDocumento(), excluirId)
                     : profesionalRepository.existsByNumeroDocumento(p.getNumeroDocumento())) {
            duplicados.add("Este profesional ya está registrado con ese número de documento");
        }
        if (editando ? profesionalRepository.existsByCorreoAndIdNot(p.getCorreo(), excluirId)
                     : profesionalRepository.existsByCorreo(p.getCorreo())) {
            duplicados.add("Este correo ya está en uso");
        }
        if (editando
                ? profesionalRepository.existsByProveedorNitAndNombresIgnoreCaseAndApellidosIgnoreCaseAndIdNot(
                        nit, p.getNombres(), p.getApellidos(), excluirId)
                : profesionalRepository.existsByProveedorNitAndNombresIgnoreCaseAndApellidosIgnoreCase(
                        nit, p.getNombres(), p.getApellidos())) {
            duplicados.add("Ya existe un profesional con este nombre");
        }
        if (editando ? profesionalRepository.existsByProveedorNitAndTelefonoAndIdNot(nit, p.getTelefono(), excluirId)
                     : profesionalRepository.existsByProveedorNitAndTelefono(nit, p.getTelefono())) {
            duplicados.add("Este teléfono ya está en uso");
        }
        if (!duplicados.isEmpty()) {
            throw new RegistroProfesionalException(RegistroProfesionalException.Tipo.DUPLICADO, duplicados);
        }
    }

    // ---- Contrato ConsultaProfesionales (lo usa agendamiento) ----

    @Override
    public Optional<ProfesionalResumen> obtenerProfesional(String profesionalId) {
        if (profesionalId == null) {
            return Optional.empty();
        }
        // Proyección: no carga la foto (antes findById traía los bytes solo para leer el nombre)
        return profesionalRepository.buscarResumen(profesionalId);
    }

    /** Para servir la imagen en GET /api/proveedores/{nit}/profesionales/{id}/foto. */
    public Optional<Profesional> obtenerConFoto(Long nit, String id) {
        return profesionalRepository.findByIdAndProveedorNit(id, nit).filter(p -> p.getFoto() != null);
    }

    // ---- Auxiliares ----

    private void verificarProveedor(Long nit) {
        if (nit == null || !proveedorRepository.existsById(nit)) {
            throw new RegistroProfesionalException(RegistroProfesionalException.Tipo.NO_ENCONTRADO,
                    List.of("Proveedor no encontrado."));
        }
    }

    /** Igual que el prototipo: solo dígitos y, si sobran, se quita el 57 inicial ("+57 300 112 9988" -> "3001129988"). */
    private static String normalizarTelefono(String telefono) {
        if (telefono == null) {
            return null;
        }
        String digitos = telefono.replaceAll("\\D", "");
        if (digitos.length() > 10 && digitos.startsWith("57")) {
            digitos = digitos.substring(2);
        }
        return digitos.isEmpty() ? null : digitos;
    }

    private static String limpiar(String valor) {
        return valor == null ? null : valor.trim();
    }

    private static boolean vacio(String valor) {
        return valor == null || valor.isBlank();
    }

    private ProfesionalDetalle aDetalle(Profesional p) {
        String fotoUrl = (p.getFoto() == null)
                ? null
                : "/api/proveedores/" + p.getProveedorNit() + "/profesionales/" + p.getId() + "/foto";
        return new ProfesionalDetalle(
                p.getApellidos(),
                p.getCorreo(),
                fotoUrl,
                p.getId(),
                p.getNombres(),
                p.getNumeroDocumento(),
                List.copyOf(p.getServicioProveedorIds()),
                p.getTelefono(),
                p.getTipoDocumento());
    }
}
