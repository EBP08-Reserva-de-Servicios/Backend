package com.reservas.notificaciones.repository;

import com.reservas.notificaciones.model.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    /** Defensa ante un evento repetido (el índice único es la garantía real). */
    boolean existsByReservaCodigoAndTipo(String reservaCodigo, String tipo);

    /** HU-36: las de una empresa, de la más reciente a la más antigua. */
    List<Notificacion> findByProveedorNitOrderByFechaCreacionDescIdDesc(Long proveedorNit);

    /** HU-36: cantidad sin leer para el indicador. */
    long countByProveedorNitAndLeidaFalse(Long proveedorNit);

    /** HU-36: solo se marca si es de esa empresa (la de otra empresa responde 404). */
    Optional<Notificacion> findByIdAndProveedorNit(Long id, Long proveedorNit);
}