package com.reservas.agendamiento.service;

import com.reservas.agendamiento.model.Reserva;
import com.reservas.agendamiento.repository.ReservaRepository;
import com.reservas.catalogo.api.ConsultaOferta;
import com.reservas.catalogo.api.OfertaDetalle;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final DisponibilidadService disponibilidadService;
    private final ConsultaOferta consultaOferta;

    public ReservaService(ReservaRepository reservaRepository,
                          DisponibilidadService disponibilidadService,
                          ConsultaOferta consultaOferta) {
        this.reservaRepository = reservaRepository;
        this.disponibilidadService = disponibilidadService;
        this.consultaOferta = consultaOferta;
    }

    /**
     * Mismas validaciones y en el mismo orden que Sprint 1:
     * 1) hay disponibilidad ese día  2) la oferta existe  3) no choca con otra reserva.
     */
    public Reserva agendar(Reserva reserva) {
        boolean hayDisponibilidad = !disponibilidadService
                .listarDisponibles(reserva.getIdServicioProveedor(), reserva.getFecha())
                .isEmpty();

        if (!hayDisponibilidad) {
            throw new ConflictoReservaException(
                    "La empresa ya no tiene disponibilidad para esa fecha, elige otra opcion.");
        }

        OfertaDetalle oferta = consultaOferta.obtenerOferta(reserva.getIdServicioProveedor())
                .orElseThrow(() -> new NoSuchElementException("Servicio-proveedor no encontrado."));

        int duracion = oferta.servicio().duracion();
        LocalTime horaFinNueva = reserva.getHora().plusMinutes(duracion);

        List<Reserva> reservasDelDia = reservaRepository
                .findByIdServicioProveedorAndFechaAndEstadoNot(
                        reserva.getIdServicioProveedor(),
                        reserva.getFecha(),
                        "CANCELADA");

        boolean hayChoque = reservasDelDia.stream().anyMatch(r -> {
            LocalTime finExistente = r.getHora().plusMinutes(duracion);
            return reserva.getHora().isBefore(finExistente)
                    && horaFinNueva.isAfter(r.getHora());
        });

        if (hayChoque) {
            throw new ConflictoReservaException(
                    "Ya existe una reserva en ese horario para esta empresa.");
        }

        reserva.setEstado("PENDIENTE");
        reserva.setFechaCreacion(LocalDateTime.now());
        return reservaRepository.save(reserva);
    }

    public Optional<Reserva> obtener(String codigo) {
        return reservaRepository.findById(codigo);
    }
}