package com.reservas.proveedores.repository;

import com.reservas.proveedores.model.Profesional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProfesionalRepository extends JpaRepository<Profesional, String> {

    List<Profesional> findByProveedorNit(Long proveedorNit);

    Optional<Profesional> findByIdAndProveedorNit(String id, Long proveedorNit);

    // Duplicados en todo el sistema
    boolean existsByNumeroDocumento(String numeroDocumento);

    boolean existsByCorreo(String correo);

    // Duplicados dentro del mismo proveedor (extras del prototipo)
    boolean existsByProveedorNitAndNombresIgnoreCaseAndApellidosIgnoreCase(Long proveedorNit, String nombres, String apellidos);

    boolean existsByProveedorNitAndTelefono(Long proveedorNit, String telefono);
}
