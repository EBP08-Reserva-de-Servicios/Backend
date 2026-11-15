package com.reservas.notificaciones.repository;

import com.reservas.notificaciones.model.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    /** Defensa ante un evento repetido (el índice único es la garantía real). */
    boolean existsByReservaCodigoAndTipo(String reservaCodigo, String tipo);
}
