package com.reservas.repository;

import com.reservas.model.Disponibilidad;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface DisponibilidadRepository extends JpaRepository<Disponibilidad, Long> {
    List<Disponibilidad> findByServicioProveedorIdAndFechaAndEstado(
        Long servicioProveedorId, LocalDate fecha, String estado);
}