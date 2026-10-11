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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProveedorService implements ConsultaProveedores, CuentaAutenticable {

    private static final int MAX_INTENTOS = 3;
    private static final int MINUTOS_BLOQUEO = 15;

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

        return new ProveedorDetalle(
                p.getCalificacionPromedio(),
                p.getCorreo(),
                p.getDescripcion(),
                p.getDireccion(),
                p.getHorasLimiteCancelacion(),
                municipio,
                p.getNit(),
                p.getRazonSocial(),
                p.getTelefono());
    }
}