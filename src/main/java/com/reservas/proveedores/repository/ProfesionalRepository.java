package com.reservas.proveedores.repository;

import com.reservas.proveedores.api.ProfesionalResumen;
import com.reservas.proveedores.model.Profesional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProfesionalRepository extends JpaRepository<Profesional, String> {

    List<Profesional> findByProveedorNit(Long proveedorNit);

    Optional<Profesional> findByIdAndProveedorNit(String id, Long proveedorNit);

    /**
     * Solo nombre y apellidos, SIN cargar la foto (bytea). La usa la disponibilidad del cliente:
     * con findById se traían los bytes de la foto solo para mostrar el nombre.
     */
    @Query("select new com.reservas.proveedores.api.ProfesionalResumen(p.apellidos, p.id, p.nombres) "
            + "from Profesional p where p.id = :id")
    Optional<ProfesionalResumen> buscarResumen(@Param("id") String id);

    // Duplicados en todo el sistema
    boolean existsByNumeroDocumento(String numeroDocumento);

    boolean existsByCorreo(String correo);

    // Duplicados dentro del mismo proveedor (extras del prototipo)
    boolean existsByProveedorNitAndNombresIgnoreCaseAndApellidosIgnoreCase(Long proveedorNit, String nombres, String apellidos);

    boolean existsByProveedorNitAndTelefono(Long proveedorNit, String telefono);
}
