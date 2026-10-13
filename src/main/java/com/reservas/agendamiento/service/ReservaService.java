package com.reservas.agendamiento.service;

import com.reservas.agendamiento.model.Disponibilidad;
import com.reservas.agendamiento.model.Reserva;
import com.reservas.agendamiento.repository.ReservaRepository;
import com.reservas.catalogo.api.ConsultaOferta;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
         * HU-24: agenda reservando el BLOQUE EXACTO (oferta + fecha + hora de inicio y, si llega, profesional).
         * Mismo orden y mismos textos de error que Sprint 1 (el front actual los muestra):
         * 1) hay disponibilidad ese día  2) la oferta existe  3) el bloque de esa hora sigue libre.
         * El bloque pasa a RESERVADO con una actualización condicional: si dos clientes llegan a la vez,
         * solo uno lo obtiene y el otro recibe 409. Todo en una transacción.
         */
        @Transactional
    public Reserva agendar(Reserva reserva) {
        // Nunca se aceptan del cuerpo: los pone el backend (clienteId lo pondrá el token en O3.2)
        reserva.setCodigo(null);
        reserva.setEstado(null);
        reserva.setClienteId(null);

        if (reserva.getIdServicioProveedor() == null || reserva.getFecha() == null || reserva.getHora() == null) {
            throw new IllegalArgumentException("Faltan datos de la reserva: idServicioProveedor, fecha y hora son obligatorios.");
        }

        // 1) Hay disponibilidad ese día (texto de Sprint 1)
        List<Disponibilidad> delDia = disponibilidadService
            .listarDisponibles(reserva.getIdServicioProveedor(), reserva.getFecha());
        if (delDia.isEmpty()) {
            throw new ConflictoReservaException(
                    "La empresa ya no tiene disponibilidad para esa fecha, elige otra opcion.");
        }

        // 2) La oferta existe (texto de Sprint 1)
        consultaOferta.obtenerOferta(reserva.getIdServicioProveedor())
                .orElseThrow(() -> new NoSuchElementException("Servicio-proveedor no encontrado."));

        // 3) Bloques candidatos: misma hora de inicio y, si se pidió un profesional, ese profesional
        List<Disponibilidad> candidatos = delDia.stream()
            .filter(d -> d.getHoraInicio().equals(reserva.getHora()))
            .filter(d -> reserva.getProfesionalId() == null
                || reserva.getProfesionalId().equals(d.getProfesionalId()))
            .toList();

        // 4) Reservar el primero que siga libre (actualización condicional: a prueba de dos clientes a la vez)
        Disponibilidad bloque = null;
        for (Disponibilidad candidato : candidatos) {
            if (disponibilidadService.reservarSiDisponible(candidato.getId())) {
            bloque = candidato;
            break;
            }
        }
        if (bloque == null) {
            throw new ConflictoReservaException(
                    "Ya existe una reserva en ese horario para esta empresa.");
        }

        reserva.setProfesionalId(bloque.getProfesionalId());
        reserva.setEstado("PENDIENTE");
        reserva.setFechaCreacion(LocalDateTime.now());
        return reservaRepository.save(reserva);
    }

    public Optional<Reserva> obtener(String codigo) {
        return reservaRepository.findById(codigo);
    }
}