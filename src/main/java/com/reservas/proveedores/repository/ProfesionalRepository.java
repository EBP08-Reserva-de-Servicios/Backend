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

    /** Lista del proveedor en un orden estable (por nombre): así una tarjeta no cambia de lugar al editarla. */
    List<Profesional> findByProveedorNitOrderByNombresAscApellidosAsc(Long proveedorNit);

    Optional<Profesional> findByIdAndProveedorNit(String id, Long proveedorNit);

    /** HU-26: profesionales que atienden una oferta (para regenerar sus bloques si cambia la duración). */
    @Query("select p from Profesional p where :ofertaId member of p.servicioProveedorIds")
    List<Profesional> buscarPorOferta(@Param("ofertaId") Long ofertaId);

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

    // HU-20: los mismos duplicados al editar, comparando con los DEMÁS profesionales (sin contarse a sí mismo)
    boolean existsByNumeroDocumentoAndIdNot(String numeroDocumento, String id);

    boolean existsByCorreoAndIdNot(String correo, String id);

    boolean existsByProveedorNitAndNombresIgnoreCaseAndApellidosIgnoreCaseAndIdNot(
            Long proveedorNit, String nombres, String apellidos, String id);

    boolean existsByProveedorNitAndTelefonoAndIdNot(Long proveedorNit, String telefono, String id);
}
