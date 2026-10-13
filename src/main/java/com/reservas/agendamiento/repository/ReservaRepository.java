package com.reservas.agendamiento.repository;

import com.reservas.agendamiento.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, String> {

    List<Reserva> findByIdServicioProveedorAndFechaAndEstadoNot(
            Long idServicioProveedor, LocalDate fecha, String estado);

    // HU-24: citas del cliente (UUID del token), de la más próxima a la más lejana
    List<Reserva> findByClienteIdOrderByFechaAscHoraAsc(String clienteId);
}