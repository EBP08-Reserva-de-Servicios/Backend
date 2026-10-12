package com.reservas.proveedores.service;

import com.reservas.compartido.CuentaAutenticable;
import com.reservas.compartido.CuentaResumen;
import com.reservas.proveedores.api.ConsultaProveedores;
import com.reservas.proveedores.api.ProveedorDetalle;
import com.reservas.proveedores.model.Proveedor;
import com.reservas.proveedores.repository.ProveedorRepository;
import com.reservas.ubicacion.api.ConsultaUbicaciones;
import com.reservas.ubicacion.api.MunicipioResumen;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ProveedorService implements ConsultaProveedores, CuentaAutenticable {

    private static final int MAX_INTENTOS = 3;
    private static final int MINUTOS_BLOQUEO = 15;

    // Reglas iguales a las del prototipo (el backend nunca es más estricto que la pantalla)
    private static final String REGEX_NIT = "^\\d{6,10}$";
    private static final String REGEX_10_DIGITOS = "^\\d{10}$";
    private static final String REGEX_CORREO = "^[^@\\s]+@[^@\\s]+$";        // un único @
    private static final Pattern DATA_URL_IMAGEN =
            Pattern.compile("^data:image/(png|jpe?g);base64,(.+)$", Pattern.DOTALL);
    private static final int MAX_BYTES_LOGO = 5 * 1024 * 1024;                // 5 MB

    private final ProveedorRepository proveedorRepository;
    private final ConsultaUbicaciones consultaUbicaciones;
    private final PasswordEncoder passwordEncoder;   // el único bean, de compartido.config

    public ProveedorService(ProveedorRepository proveedorRepository,
                            ConsultaUbicaciones consultaUbicaciones,
                            PasswordEncoder passwordEncoder) {
        this.proveedorRepository = proveedorRepository;
        this.consultaUbicaciones = consultaUbicaciones;
        this.passwordEncoder = passwordEncoder;
    }

    // ---- Operaciones usadas por ProveedorController (dentro del componente) ----

    public List<ProveedorDetalle> listar() {
        return proveedorRepository.findAll().stream().map(this::aDetalle).toList();
    }

    public Optional<ProveedorDetalle> obtener(Long nit) {
        return proveedorRepository.findById(nit).map(this::aDetalle);
    }

    public ProveedorDetalle crear(Proveedor proveedor) {
        return aDetalle(proveedorRepository.save(proveedor));
    }

    /** Proveedor con logo, para servir la imagen en GET /api/proveedores/{nit}/logo. */
    public Optional<Proveedor> obtenerConLogo(Long nit) {
        return proveedorRepository.findById(nit).filter(p -> p.getLogo() != null);
    }

    // ---------------------------------------------------------------
    // Registro (HU-16 y HU-Seg-01)
    // ---------------------------------------------------------------

    /**
     * Valida los datos, verifica duplicados, cifra la clave y guarda el proveedor.
     * El controller arma "proveedor" con los campos de texto; nit, logo y clave llegan aparte
     * porque hay que convertirlos. Lanza RegistroProveedorException (INVALIDO o DUPLICADO).
     */
    public ProveedorDetalle registrar(Proveedor proveedor, String nitTexto, String logoDataUrl, String clavePlana) {

        // 1) Obligatorios y formatos (todos los errores juntos)
        List<String> errores = new ArrayList<>();

        if (vacio(proveedor.getTipoServicio()))  errores.add("El tipo de servicios es obligatorio.");
        if (vacio(proveedor.getRazonSocial()))   errores.add("El nombre de la empresa es obligatorio.");

        if (vacio(nitTexto)) {
            errores.add("El NIT es obligatorio.");
        } else if (!nitTexto.matches(REGEX_NIT)) {
            errores.add("El NIT debe tener entre 6 y 10 dígitos");
        }

        if (vacio(proveedor.getTelefono())) {
            errores.add("El teléfono es obligatorio.");
        } else if (!proveedor.getTelefono().matches(REGEX_10_DIGITOS)) {
            errores.add("El teléfono debe tener 10 dígitos numéricos.");
        }

        if (vacio(proveedor.getCorreo())) {
            errores.add("El correo es obligatorio.");
        } else if (!proveedor.getCorreo().matches(REGEX_CORREO)) {
            errores.add("El correo electrónico no es válido.");
        }

        if (vacio(proveedor.getRepresentanteNombres()))   errores.add("El nombre es obligatorio.");
        if (vacio(proveedor.getRepresentanteApellidos())) errores.add("El apellido es obligatorio.");
        if (vacio(proveedor.getRepresentanteCelular())) {
            errores.add("El celular es obligatorio.");
        } else if (!proveedor.getRepresentanteCelular().matches(REGEX_10_DIGITOS)) {
            errores.add("El celular debe tener 10 dígitos numéricos.");
        }
        if (vacio(proveedor.getRepresentanteDocumento())) errores.add("El número de documento es obligatorio.");

        // Logo: obligatorio, PNG o JPG, máximo 5 MB
        byte[] logoBytes = null;
        String logoTipo = null;
        if (vacio(logoDataUrl)) {
            errores.add("El logo es obligatorio.");
        } else {
            Matcher m = DATA_URL_IMAGEN.matcher(logoDataUrl);
            if (m.matches()) {
                try {
                    logoBytes = Base64.getDecoder().decode(m.group(2).trim());
                    logoTipo = m.group(1).equals("png") ? "image/png" : "image/jpeg";
                } catch (IllegalArgumentException e) {
                    logoBytes = null;                       // base64 mal formado
                }
            }
            if (logoBytes == null || logoBytes.length == 0 || logoBytes.length > MAX_BYTES_LOGO) {
                errores.add("Sube una imagen en formato PNG o JPG");
            }
        }

        // Contraseña: las 5 reglas SOLO si hay clave (evita NullPointerException)
        if (vacio(clavePlana)) {
            errores.add("La contraseña es obligatoria.");
        } else {
            if (clavePlana.length() < 8)                 errores.add("La contraseña debe tener al menos 8 caracteres.");
            if (!clavePlana.matches(".*[A-Z].*"))        errores.add("La contraseña debe incluir al menos una letra mayúscula.");
            if (!clavePlana.matches(".*[a-z].*"))        errores.add("La contraseña debe incluir al menos una letra minúscula.");
            if (!clavePlana.matches(".*\\d.*"))          errores.add("La contraseña debe incluir al menos un número.");
            if (!clavePlana.matches(".*[^A-Za-z0-9].*")) errores.add("La contraseña debe incluir al menos un carácter especial.");
        }

        if (!errores.isEmpty()) {
            throw new RegistroProveedorException(RegistroProveedorException.Tipo.INVALIDO, errores);
        }

        // 2) Duplicados (solo si los datos ya son válidos). Textos exactos del prototipo.
        Long nit = Long.valueOf(nitTexto);
        String logoHash = sha256(logoBytes);

        List<String> duplicados = new ArrayList<>();
        if (proveedorRepository.existsByCorreo(proveedor.getCorreo()))           duplicados.add("Este correo ya está en uso");
        if (proveedorRepository.existsByTelefono(proveedor.getTelefono()))       duplicados.add("Este teléfono ya está en uso");
        if (proveedorRepository.existsById(nit))                                 duplicados.add("Este NIT ya está en uso");
        if (proveedorRepository.existsByRazonSocial(proveedor.getRazonSocial())) duplicados.add("Este nombre de empresa ya está en uso");
        if (proveedorRepository.existsByLogoHash(logoHash))                      duplicados.add("Este logo ya está en uso. Sube un logo diferente");

        if (!duplicados.isEmpty()) {
            throw new RegistroProveedorException(RegistroProveedorException.Tipo.DUPLICADO, duplicados);
        }

        // 3) Completar y guardar con la clave cifrada (HU-Seg-01)
        proveedor.setNit(nit);
        proveedor.setLogo(logoBytes);
        proveedor.setLogoTipo(logoTipo);
        proveedor.setLogoHash(logoHash);
        proveedor.setClave(passwordEncoder.encode(clavePlana));
        proveedor.setIntentosFallidos(0);
        proveedor.setBloqueadoHasta(null);

        return aDetalle(proveedorRepository.save(proveedor));
    }

    private static boolean vacio(String valor) {
        return valor == null || valor.isBlank();
    }

    private static String sha256(byte[] datos) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(datos));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);   // no pasa en Java 21
        }
    }

    // ---- Contrato ConsultaProveedores (usado por otros componentes) ----

    @Override
    public Optional<ProveedorDetalle> obtenerProveedor(Long nit) {
        return obtener(nit);
    }

    // ---- Contrato CuentaAutenticable (lo usa el login en A2) ----

    @Override
    public String rol() {
        return "PROVEEDOR";
    }

    @Override
    public Optional<CuentaResumen> buscarPorCorreo(String correo) {
        return proveedorRepository.findByCorreo(correo)
                .filter(p -> p.getClave() != null)   // precargados sin clave: no son cuentas
                .map(p -> new CuentaResumen(p.getNit().toString(), p.getRazonSocial(), p.getCorreo(), rol()));
    }

    @Override
    public boolean validarCredenciales(String correo, String clave) {
        if (clave == null) return false;             // matches(null, ...) lanzaría excepción
        return proveedorRepository.findByCorreo(correo)
                .map(p -> p.getClave() != null && passwordEncoder.matches(clave, p.getClave()))
                .orElse(false);
    }

    @Override
    public boolean estaBloqueada(String correo) {
        return proveedorRepository.findByCorreo(correo)
                .map(p -> p.getBloqueadoHasta() != null && p.getBloqueadoHasta().isAfter(LocalDateTime.now()))
                .orElse(false);
    }

        @Override
    public Optional<LocalDateTime> bloqueadaHasta(String correo) {
        return proveedorRepository.findByCorreo(correo)
                .map(p -> p.getBloqueadoHasta())
                .filter(hasta -> hasta.isAfter(LocalDateTime.now()));
    }

    @Override
    public void registrarIntentoFallido(String correo) {
        proveedorRepository.findByCorreo(correo).ifPresent(p -> {
            // Si ya había un bloqueo y venció, se empieza a contar de nuevo (HU-Seg-02: intentos consecutivos)
            if (p.getBloqueadoHasta() != null && !p.getBloqueadoHasta().isAfter(LocalDateTime.now())) {
                p.setIntentosFallidos(0);
                p.setBloqueadoHasta(null);
            }
            int intentos = (p.getIntentosFallidos() == null ? 0 : p.getIntentosFallidos()) + 1;
            p.setIntentosFallidos(intentos);
            if (intentos >= MAX_INTENTOS) {
                p.setBloqueadoHasta(LocalDateTime.now().plusMinutes(MINUTOS_BLOQUEO));
            }
            proveedorRepository.save(p);
        });
    }

    @Override
    public void registrarIntentoExitoso(String correo) {
        proveedorRepository.findByCorreo(correo).ifPresent(p -> {
            p.setIntentosFallidos(0);
            p.setBloqueadoHasta(null);
            proveedorRepository.save(p);
        });
    }

    // ---- Conversión entidad -> vista pública ----

    private ProveedorDetalle aDetalle(Proveedor p) {
        MunicipioResumen municipio = (p.getMunicipioId() == null)
                ? null
                : consultaUbicaciones.obtenerMunicipio(p.getMunicipioId()).orElse(null);

        // Solo la dirección del logo; los bytes se sirven aparte en GET /api/proveedores/{nit}/logo
        String logoUrl = (p.getLogo() == null) ? null : "/api/proveedores/" + p.getNit() + "/logo";

        return new ProveedorDetalle(
                p.getCalificacionPromedio(),
                p.getCorreo(),
                p.getDescripcion(),
                p.getDireccion(),
                p.getHorasLimiteCancelacion(),
                logoUrl,
                municipio,
                p.getNit(),
                p.getRazonSocial(),
                p.getTelefono());
    }
}