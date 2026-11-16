package com.reservas.agendamiento.service;

import com.reservas.agendamiento.api.ConsultaReservas;
import com.reservas.agendamiento.api.ReservaResumen;
import com.reservas.agendamiento.model.Reserva;
import com.reservas.agendamiento.repository.ReservaRepository;
import com.reservas.catalogo.api.ConsultaOferta;
import com.reservas.catalogo.api.OfertaDetalle;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HU-36: implementa ConsultaReservas para que notificaciones muestre los datos de cada cita.
 * Clase aparte (no dentro de ReservaService) para no tocar el flujo de agendar.
 */
@Service
public class ConsultaReservasService implements ConsultaReservas {

    private final ReservaRepository reservaRepository;
    private final ConsultaOferta consultaOferta;

    public ConsultaReservasService(ReservaRepository reservaRepository, ConsultaOferta consultaOferta) {
        this.reservaRepository = reservaRepository;
        this.consultaOferta = consultaOferta;
    }

    @Override
    public List<ReservaResumen> obtenerReservas(Collection<String> codigos) {
        if (codigos == null || codigos.isEmpty()) {
            return List.of();
        }
        Map<Long, OfertaDetalle> ofertas = new HashMap<>();        // solo para reservas antiguas sin hora_fin
        return reservaRepository.findAllById(codigos).stream()
                .map(r -> new ReservaResumen(r.getClienteId(), r.getCodigo(), r.getEstado(), r.getFecha(),
                        r.getHora(), horaFin(r, ofertas), r.getIdServicioProveedor(), r.getProfesionalId()))
                .toList();
    }

    /** La guardada al agendar; si es una reserva antigua (null), inicio + duración actual de la oferta. */
    private LocalTime horaFin(Reserva r, Map<Long, OfertaDetalle> ofertas) {
        if (r.getHoraFin() != null) {
            return r.getHoraFin();
        }
        if (r.getHora() == null || r.getIdServicioProveedor() == null) {
            return null;
        }
        OfertaDetalle oferta = ofertas.computeIfAbsent(r.getIdServicioProveedor(),
                id -> consultaOferta.obtenerOferta(id).orElse(null));
        if (oferta == null) {
            return null;
        }
        Integer duracion = (oferta.duracion() != null) ? oferta.duracion()
                : (oferta.servicio() != null ? oferta.servicio().duracion() : null);
        return (duracion == null) ? null : r.getHora().plusMinutes(duracion);
    }
}