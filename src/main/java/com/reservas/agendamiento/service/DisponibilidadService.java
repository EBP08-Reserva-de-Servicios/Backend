package com.reservas.agendamiento.service;

import com.reservas.agendamiento.api.DisponibilidadDetalle;
import com.reservas.agendamiento.model.Disponibilidad;
import com.reservas.agendamiento.repository.DisponibilidadRepository;
import com.reservas.catalogo.api.ConsultaOferta;
import com.reservas.catalogo.api.OfertaDetalle;
import com.reservas.proveedores.api.ConsultaProfesionales;
import com.reservas.proveedores.api.ProfesionalResumen;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class DisponibilidadService {

    private static final String DISPONIBLE = "DISPONIBLE";

    private final DisponibilidadRepository disponibilidadRepository;
    private final ConsultaOferta consultaOferta;
    private final ConsultaProfesionales consultaProfesionales;

    public DisponibilidadService(DisponibilidadRepository disponibilidadRepository,
                                 ConsultaOferta consultaOferta,
                                 ConsultaProfesionales consultaProfesionales) {
        this.disponibilidadRepository = disponibilidadRepository;
        this.consultaOferta = consultaOferta;
        this.consultaProfesionales = consultaProfesionales;
    }

    /** Bloques DISPONIBLES de una oferta en una fecha (entidades, para uso dentro de agendamiento). */
    public List<Disponibilidad> listarDisponibles(Long servicioProveedorId, LocalDate fecha) {
        return disponibilidadRepository
                .findByServicioProveedorIdAndFechaAndEstado(servicioProveedorId, fecha, DISPONIBLE);
    }

    /** Lo mismo, como vista pública con la oferta anidada (para el controller). */
    public List<DisponibilidadDetalle> consultarDisponibles(Long servicioProveedorId, LocalDate fecha) {
        List<Disponibilidad> bloques = listarDisponibles(servicioProveedorId, fecha);
        if (bloques.isEmpty()) {
            return List.of();
        }
        // Todos los bloques son de la misma oferta: se consulta una sola vez
        OfertaDetalle oferta = consultaOferta.obtenerOferta(servicioProveedorId).orElse(null);
        return bloques.stream().map(d -> aDetalle(d, oferta)).toList();
    }

    public DisponibilidadDetalle crear(Disponibilidad disponibilidad) {
        Disponibilidad guardada = disponibilidadRepository.save(disponibilidad);
        OfertaDetalle oferta = (guardada.getServicioProveedorId() == null)
                ? null
                : consultaOferta.obtenerOferta(guardada.getServicioProveedorId()).orElse(null);
        return aDetalle(guardada, oferta);
    }

    private DisponibilidadDetalle aDetalle(Disponibilidad d, OfertaDetalle oferta) {
        ProfesionalResumen profesional = (d.getProfesionalId() == null)
            ? null
            : consultaProfesionales.obtenerProfesional(d.getProfesionalId()).orElse(null);
        return new DisponibilidadDetalle(
            d.getEstado(), d.getFecha(), d.getHoraFin(), d.getHoraInicio(), d.getId(), profesional, oferta);
    }
}