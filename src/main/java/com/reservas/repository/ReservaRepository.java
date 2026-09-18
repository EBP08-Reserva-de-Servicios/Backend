package com.reservas.repository;

import com.reservas.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, String> {

    List<Reserva> findByIdServicioProveedorAndFechaAndEstadoNot(
            Long idServicioProveedor, LocalDate fecha, String estado);
}