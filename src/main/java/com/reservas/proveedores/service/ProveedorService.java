package com.reservas.proveedores.service;

import com.reservas.proveedores.api.ConsultaProveedores;
import com.reservas.proveedores.api.ProveedorDetalle;
import com.reservas.proveedores.model.Proveedor;
import com.reservas.proveedores.repository.ProveedorRepository;
import com.reservas.ubicacion.api.ConsultaUbicaciones;
import com.reservas.ubicacion.api.MunicipioResumen;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProveedorService implements ConsultaProveedores {

    private final ProveedorRepository proveedorRepository;
    private final ConsultaUbicaciones consultaUbicaciones;

    public ProveedorService(ProveedorRepository proveedorRepository,
                            ConsultaUbicaciones consultaUbicaciones) {
        this.proveedorRepository = proveedorRepository;
        this.consultaUbicaciones = consultaUbicaciones;
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