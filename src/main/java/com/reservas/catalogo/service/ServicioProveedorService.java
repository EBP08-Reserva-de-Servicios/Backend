package com.reservas.catalogo.service;

import com.reservas.catalogo.api.ConsultaOferta;
import com.reservas.catalogo.api.OfertaDetalle;
import com.reservas.catalogo.api.ServicioResumen;
import com.reservas.catalogo.model.Servicio;
import com.reservas.catalogo.model.ServicioProveedor;
import com.reservas.catalogo.repository.ServicioProveedorRepository;
import com.reservas.catalogo.repository.ServicioRepository;
import com.reservas.proveedores.api.ConsultaProveedores;
import com.reservas.proveedores.api.ProveedorDetalle;
import com.reservas.proveedores.api.VerificadorOfertas;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
public class ServicioProveedorService implements ConsultaOferta, VerificadorOfertas {

    private final ServicioProveedorRepository servicioProveedorRepository;
    private final ServicioRepository servicioRepository;
    private final ConsultaProveedores consultaProveedores;

    public ServicioProveedorService(ServicioProveedorRepository servicioProveedorRepository,
                                    ServicioRepository servicioRepository,
                                    ConsultaProveedores consultaProveedores) {
        this.servicioProveedorRepository = servicioProveedorRepository;
        this.servicioRepository = servicioRepository;
        this.consultaProveedores = consultaProveedores;
    }

    // ---- Operaciones usadas por los controllers del componente ----

    /** Sprint 1: POST /api/servicio-proveedor (sin cambios). */
    public OfertaDetalle publicar(ServicioProveedor servicioProveedor) {
        return aDetalle(servicioProveedorRepository.save(servicioProveedor));
    }

    /**
     * HU-19: el proveedor publica un servicio del catálogo con precio, duración y descripción propios.
     * Validaciones nunca más estrictas que el prototipo; textos del prototipo cuando existen.
     */
    public OfertaDetalle publicarDelProveedor(Long nit, ServicioProveedor oferta, Long servicioId) {
        verificarProveedor(nit);

        List<String> errores = new ArrayList<>();

        Servicio servicio = null;
        if (servicioId == null) {
            errores.add("El servicio es obligatorio.");
        } else {
            servicio = servicioRepository.findById(servicioId).orElse(null);
            if (servicio == null) {
                errores.add("El servicio no existe.");
            }
        }

        if (oferta.getPrecio() == null) {
            errores.add("El precio es obligatorio.");
        } else if (oferta.getPrecio() < 0) {
            errores.add("El precio no puede ser negativo.");
        }

        if (oferta.getDuracion() == null) {
            errores.add("La duración es obligatoria.");
        } else if (oferta.getDuracion() <= 0) {
            errores.add("Ingresa un número de minutos mayor a 0");
        }

        if (oferta.getDescripcion() == null || oferta.getDescripcion().isBlank()) {
            errores.add("La descripción es obligatoria");
        }

        if (!errores.isEmpty()) {
            throw new RegistroOfertaException(RegistroOfertaException.Tipo.INVALIDO, errores);
        }

        oferta.setProveedorNit(nit);
        oferta.setServicio(servicio);
        oferta.setDescripcion(oferta.getDescripcion().trim());
        return aDetalle(servicioProveedorRepository.save(oferta));
    }

    /** HU-19 escenario 3: catálogo de servicios del proveedor. */
    public List<OfertaDetalle> listarDelProveedor(Long nit) {
        verificarProveedor(nit);
        return servicioProveedorRepository.findByProveedorNit(nit).stream().map(this::aDetalle).toList();
    }

    private void verificarProveedor(Long nit) {
        if (consultaProveedores.obtenerProveedor(nit).isEmpty()) {
            throw new RegistroOfertaException(RegistroOfertaException.Tipo.NO_ENCONTRADO,
                    List.of("Proveedor no encontrado."));
        }
    }

    // ---- Contrato VerificadorOfertas (lo usa proveedores para HU-29) ----

    @Override
    public boolean pertenecenAlProveedor(Long nit, List<Long> servicioProveedorIds) {
        if (nit == null || servicioProveedorIds == null || servicioProveedorIds.isEmpty()) {
            return false;
        }
        List<ServicioProveedor> encontradas = servicioProveedorRepository.findAllById(servicioProveedorIds);
        return encontradas.size() == new HashSet<>(servicioProveedorIds).size()
                && encontradas.stream().allMatch(sp -> nit.equals(sp.getProveedorNit()));
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