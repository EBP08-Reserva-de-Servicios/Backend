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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    /** HU-24: true si el bloque seguía DISPONIBLE y quedó RESERVADO; false si otro lo tomó primero. */
    public boolean reservarSiDisponible(Long disponibilidadId) {
        return disponibilidadRepository.reservarSiDisponible(disponibilidadId) == 1;
    }

    /** Lo mismo, como vista pública con la oferta anidada (para el controller). */
    public List<DisponibilidadDetalle> consultarDisponibles(Long servicioProveedorId, LocalDate fecha) {
        return aDetalles(servicioProveedorId, listarDisponibles(servicioProveedorId, fecha));
    }

    /**
     * Mejora de rendimiento: todos los bloques DISPONIBLES de una oferta entre dos fechas (incluidas),
     * ordenados por fecha y hora, en UNA sola consulta a la base (antes el front hacía una petición por día).
     */
    public List<DisponibilidadDetalle> consultarDisponiblesEnRango(Long servicioProveedorId,
                                                                   LocalDate desde, LocalDate hasta) {
        List<Disponibilidad> bloques = disponibilidadRepository
                .findByServicioProveedorIdAndEstadoAndFechaBetweenOrderByFechaAscHoraInicioAsc(
                        servicioProveedorId, DISPONIBLE, desde, hasta);
        return aDetalles(servicioProveedorId, bloques);
    }

    public DisponibilidadDetalle crear(Disponibilidad disponibilidad) {
        Disponibilidad guardada = disponibilidadRepository.save(disponibilidad);
        OfertaDetalle oferta = (guardada.getServicioProveedorId() == null)
                ? null
                : consultaOferta.obtenerOferta(guardada.getServicioProveedorId()).orElse(null);
        return aDetalle(guardada, oferta, new HashMap<>());
    }

    // ---- Conversión entidad -> vista pública ----

    /**
     * Todos los bloques son de la misma oferta: la oferta se consulta UNA vez y cada profesional UNA vez
     * (antes se consultaba el profesional por cada bloque).
     */
    private List<DisponibilidadDetalle> aDetalles(Long servicioProveedorId, List<Disponibilidad> bloques) {
        if (bloques.isEmpty()) {
            return List.of();
        }
        OfertaDetalle oferta = consultaOferta.obtenerOferta(servicioProveedorId).orElse(null);
        Map<String, Optional<ProfesionalResumen>> profesionales = new HashMap<>();
        return bloques.stream().map(d -> aDetalle(d, oferta, profesionales)).toList();
    }

    private DisponibilidadDetalle aDetalle(Disponibilidad d, OfertaDetalle oferta,
                                           Map<String, Optional<ProfesionalResumen>> profesionales) {
        ProfesionalResumen profesional = (d.getProfesionalId() == null)
                ? null
                : profesionales.computeIfAbsent(d.getProfesionalId(), consultaProfesionales::obtenerProfesional)
                        .orElse(null);
        return new DisponibilidadDetalle(
                d.getEstado(), d.getFecha(), d.getHoraFin(), d.getHoraInicio(), d.getId(), profesional, oferta);
    }
}