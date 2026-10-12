package com.reservas.catalogo.service;

import com.reservas.catalogo.api.ConsultaOferta;
import com.reservas.catalogo.api.OfertaDetalle;
import com.reservas.catalogo.api.ServicioResumen;
import com.reservas.catalogo.model.Servicio;
import com.reservas.catalogo.model.ServicioProveedor;
import com.reservas.catalogo.repository.ServicioProveedorRepository;
import com.reservas.proveedores.api.ConsultaProveedores;
import com.reservas.proveedores.api.ProveedorDetalle;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ServicioProveedorService implements ConsultaOferta {

    private final ServicioProveedorRepository servicioProveedorRepository;
    private final ConsultaProveedores consultaProveedores;

    public ServicioProveedorService(ServicioProveedorRepository servicioProveedorRepository,
                                    ConsultaProveedores consultaProveedores) {
        this.servicioProveedorRepository = servicioProveedorRepository;
        this.consultaProveedores = consultaProveedores;
    }

    // ---- Operaciones usadas por ServicioProveedorController (dentro del componente) ----

    public OfertaDetalle publicar(ServicioProveedor servicioProveedor) {
        return aDetalle(servicioProveedorRepository.save(servicioProveedor));
    }

    // ---- Contrato ConsultaOferta (usado por otros componentes) ----

    @Override
    public Optional<OfertaDetalle> obtenerOferta(Long servicioProveedorId) {
        return servicioProveedorRepository.findById(servicioProveedorId).map(this::aDetalle);
    }

    @Override
    public List<OfertaDetalle> listarOfertas() {
        return servicioProveedorRepository.findAll().stream().map(this::aDetalle).toList();
    }

    @Override
    public List<OfertaDetalle> listarOfertasPorServicio(Long servicioId) {
        return servicioProveedorRepository.findByServicioId(servicioId).stream().map(this::aDetalle).toList();
    }

    // ---- Conversión entidad -> vista pública ----

    private OfertaDetalle aDetalle(ServicioProveedor sp) {
        ProveedorDetalle proveedor = (sp.getProveedorNit() == null)
                ? null
                : consultaProveedores.obtenerProveedor(sp.getProveedorNit()).orElse(null);

        return new OfertaDetalle(sp.getDescripcion(), sp.getDuracion(), sp.getId(), sp.getPrecio(),
                proveedor, aResumen(sp.getServicio()));
    }

    private ServicioResumen aResumen(Servicio s) {
        if (s == null) {
            return null;
        }
        return new ServicioResumen(s.getDescripcion(), s.getDuracion(), s.getId(), s.getNombre(), s.getPrecio());
    }
}