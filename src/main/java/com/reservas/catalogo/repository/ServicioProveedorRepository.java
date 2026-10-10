package com.reservas.catalogo.repository;

import com.reservas.catalogo.model.ServicioProveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ServicioProveedorRepository extends JpaRepository<ServicioProveedor, Long> {
    List<ServicioProveedor> findByServicioId(Long servicioId);
}