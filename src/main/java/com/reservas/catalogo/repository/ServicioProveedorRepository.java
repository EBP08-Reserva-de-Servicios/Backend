package com.reservas.catalogo.repository;

import com.reservas.catalogo.model.ServicioProveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ServicioProveedorRepository extends JpaRepository<ServicioProveedor, Long> {
    List<ServicioProveedor> findByServicioId(Long servicioId);

    /** HU-19: ofertas publicadas por un proveedor. */
    List<ServicioProveedor> findByProveedorNit(Long proveedorNit);

    /** HU-26: la oferta solo se edita si es de ese proveedor. */
    Optional<ServicioProveedor> findByIdAndProveedorNit(Long id, Long proveedorNit);
}