package com.reservas.agendamiento.service;

import com.reservas.agendamiento.model.Reserva;
import com.reservas.agendamiento.repository.ReservaRepository;
import com.reservas.catalogo.api.ConsultaOferta;
import com.reservas.catalogo.api.OfertaDetalle;
import com.reservas.proveedores.api.CitaDelProfesional;
import com.reservas.proveedores.api.ConsultaCitasDelProfesional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * HU-21: le responde a proveedores qué citas activas tiene un profesional, para no dejar citas
 * por fuera al editar o borrar su horario. La hora de fin es la guardada al agendar (hora_fin);
 * en reservas anteriores a ese cambio se calcula con la duración de la oferta.
 */
@Service
public class CitasDelProfesionalService implements ConsultaCitasDelProfesional {

    private static final String CANCELADA = "CANCELADA";

    private final ReservaRepository reservaRepository;
    private final ConsultaOferta consultaOferta;

    public CitasDelProfesionalService(ReservaRepository reservaRepository, ConsultaOferta consultaOferta) {
        this.reservaRepository = reservaRepository;
        this.consultaOferta = consultaOferta;
    }

    @Override
    public List<CitaDelProfesional> activasDesde(String profesionalId, LocalDate desde) {
        Map<Long, Optional<OfertaDetalle>> ofertas = new HashMap<>();   // cada oferta se consulta una vez
        return reservaRepository
                .findByProfesionalIdAndFechaGreaterThanEqualAndEstadoNotOrderByFechaAscHoraAsc(
                        profesionalId, desde, CANCELADA)
                .stream()
                .map(r -> new CitaDelProfesional(r.getCodigo(), r.getFecha(), r.getHora(), horaFin(r, ofertas)))
                .toList();
    }

    private LocalTime horaFin(Reserva r, Map<Long, Optional<OfertaDetalle>> ofertas) {
        if (r.getHoraFin() != null) {
            return r.getHoraFin();
        }
        if (r.getHora() == null || r.getIdServicioProveedor() == null) {
            return null;
        }
        OfertaDetalle oferta = ofertas
                .computeIfAbsent(r.getIdServicioProveedor(), consultaOferta::obtenerOferta)
                .orElse(null);
        if (oferta == null) {
            return null;
        }
        Integer duracion = (oferta.duracion() != null) ? oferta.duracion()
                : (oferta.servicio() != null ? oferta.servicio().duracion() : null);
        return (duracion == null) ? null : r.getHora().plusMinutes(duracion);
    }
}
