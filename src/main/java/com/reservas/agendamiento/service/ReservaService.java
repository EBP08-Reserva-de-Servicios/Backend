package com.reservas.agendamiento.service;

import com.reservas.agendamiento.api.ReservaDetalle;
import com.reservas.agendamiento.model.Disponibilidad;
import com.reservas.agendamiento.model.Reserva;
import com.reservas.agendamiento.repository.ReservaRepository;
import com.reservas.catalogo.api.ConsultaOferta;
import com.reservas.catalogo.api.OfertaDetalle;
import com.reservas.proveedores.api.ConsultaProfesionales;
import com.reservas.proveedores.api.ProfesionalResumen;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final DisponibilidadService disponibilidadService;
    private final ConsultaOferta consultaOferta;
    private final ConsultaProfesionales consultaProfesionales;

    public ReservaService(ReservaRepository reservaRepository,
                          DisponibilidadService disponibilidadService,
                          ConsultaOferta consultaOferta,
                          ConsultaProfesionales consultaProfesionales) {
        this.reservaRepository = reservaRepository;
        this.disponibilidadService = disponibilidadService;
        this.consultaOferta = consultaOferta;
        this.consultaProfesionales = consultaProfesionales;
    }

    /**
     * HU-24: agenda reservando el BLOQUE EXACTO (oferta + fecha + hora de inicio y, si llega, profesional).
     * Mismo orden y mismos textos de error que Sprint 1 (el front actual los muestra):
     * 1) hay disponibilidad ese día  2) la oferta existe  3) el bloque de esa hora sigue libre.
     * El bloque pasa a RESERVADO con una actualización condicional: si dos clientes llegan a la vez,
     * solo uno lo obtiene y el otro recibe 409. Todo en una transacción.
     *
     * @param clienteId UUID del cliente tomado del token (sub). Desde el contraer (16 oct) siempre llega:
     *                  SecurityConfig solo deja agendar con token de CLIENTE (sin token 401, proveedor 403).
     */
    @Transactional
    public Reserva agendar(Reserva reserva, String clienteId) {
        // Defensa extra: SecurityConfig ya lo garantiza, pero el service no confía en que lo llamen bien.
        if (clienteId == null) {
            throw new IllegalArgumentException("Inicia sesión como cliente para agendar.");
        }
        // Nunca se aceptan del cuerpo: los pone el backend
        reserva.setCodigo(null);
        reserva.setEstado(null);
        reserva.setClienteId(clienteId);
        reserva.setIdCliente(null); // contraer: el idCliente del Sprint 1 ya no se usa (el cliente sale del token)

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

    /** HU-24 "Mis citas": las reservas del cliente del token, con oferta y profesional anidados. */
    public List<ReservaDetalle> listarDelCliente(String clienteId) {
        Map<Long, OfertaDetalle> ofertas = new HashMap<>();       // cada oferta se consulta una sola vez
        Map<String, ProfesionalResumen> profesionales = new HashMap<>();
        return reservaRepository.findByClienteIdOrderByFechaAscHoraAsc(clienteId).stream()
                .map(r -> {
                    OfertaDetalle oferta = (r.getIdServicioProveedor() == null)
                            ? null
                            : ofertas.computeIfAbsent(r.getIdServicioProveedor(),
                                    id -> consultaOferta.obtenerOferta(id).orElse(null));
                    ProfesionalResumen profesional = (r.getProfesionalId() == null)
                            ? null
                            : profesionales.computeIfAbsent(r.getProfesionalId(),
                                    id -> consultaProfesionales.obtenerProfesional(id).orElse(null));
                    return new ReservaDetalle(r.getCodigo(), r.getEstado(), r.getFecha(), r.getHora(),
                            horaFin(r.getHora(), oferta), profesional, oferta);
                })
                .toList();
    }

    /** Hora de fin = inicio + duración de la oferta (o, si no tiene, la del servicio). Null si no se puede saber. */
    private static LocalTime horaFin(LocalTime hora, OfertaDetalle oferta) {
        if (hora == null || oferta == null) {
            return null;
        }
        Integer duracion = (oferta.duracion() != null) ? oferta.duracion()
                : (oferta.servicio() != null ? oferta.servicio().duracion() : null);
        return (duracion == null) ? null : hora.plusMinutes(duracion);
    }
}