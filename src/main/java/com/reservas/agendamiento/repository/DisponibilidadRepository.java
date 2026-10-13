package com.reservas.agendamiento.repository;

import com.reservas.agendamiento.model.Disponibilidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface DisponibilidadRepository extends JpaRepository<Disponibilidad, Long> {
    List<Disponibilidad> findByServicioProveedorIdAndFechaAndEstado(
        Long servicioProveedorId, LocalDate fecha, String estado);

    // HU-32: regeneración de los bloques de un profesional
    List<Disponibilidad> findByProfesionalIdAndFechaGreaterThanEqual(String profesionalId, LocalDate fecha);

    long deleteByProfesionalIdAndEstadoAndFechaGreaterThanEqual(String profesionalId, String estado, LocalDate fecha);

    /**
     * HU-24: reserva el bloque SOLO si sigue libre, en una sola sentencia (actualización condicional).
     * Devuelve 1 si lo reservó, 0 si otro cliente lo tomó primero. Así no hay doble reserva.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Disponibilidad d set d.estado = 'RESERVADO' where d.id = :id and d.estado = 'DISPONIBLE'")
    int reservarSiDisponible(@Param("id") Long id);
}