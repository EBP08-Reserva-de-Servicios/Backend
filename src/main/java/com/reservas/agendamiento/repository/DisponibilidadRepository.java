package com.reservas.agendamiento.repository;

import com.reservas.agendamiento.model.Disponibilidad;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface DisponibilidadRepository extends JpaRepository<Disponibilidad, Long> {
    List<Disponibilidad> findByServicioProveedorIdAndFechaAndEstado(
        Long servicioProveedorId, LocalDate fecha, String estado);

    // HU-32: regeneración de los bloques de un profesional
    List<Disponibilidad> findByProfesionalIdAndFechaGreaterThanEqual(String profesionalId, LocalDate fecha);

    long deleteByProfesionalIdAndEstadoAndFechaGreaterThanEqual(String profesionalId, String estado, LocalDate fecha);
}