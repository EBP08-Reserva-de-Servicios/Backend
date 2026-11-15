package com.reservas.agendamiento.service;

import com.reservas.agendamiento.model.Disponibilidad;
import com.reservas.agendamiento.repository.DisponibilidadRepository;
import com.reservas.catalogo.api.ConsultaOferta;
import com.reservas.catalogo.api.OfertaDetalle;
import com.reservas.proveedores.api.DiaSemana;
import com.reservas.proveedores.api.FranjaHorarioResumen;
import com.reservas.proveedores.api.GeneradorDisponibilidad;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HU-32 -> calendario del cliente: convierte el horario semanal de un profesional en bloques
 * de disponibilidad con fecha, para las próximas SEMANAS semanas (desde mañana).
 * Bloques del tamaño de la duración de la oferta (si es null, la del servicio del catálogo).
 */
@Service
public class GeneracionDisponibilidadService implements GeneradorDisponibilidad {

    static final int SEMANAS = 4;
    private static final String DISPONIBLE = "DISPONIBLE";

    private final DisponibilidadRepository disponibilidadRepository;
    private final ConsultaOferta consultaOferta;

    public GeneracionDisponibilidadService(DisponibilidadRepository disponibilidadRepository,
                                           ConsultaOferta consultaOferta) {
        this.disponibilidadRepository = disponibilidadRepository;
        this.consultaOferta = consultaOferta;
    }

    @Override
    @Transactional
    public int regenerar(String profesionalId, List<Long> servicioProveedorIds, List<FranjaHorarioResumen> franjas) {
        LocalDate desde = LocalDate.now().plusDays(1);
        LocalDate hasta = desde.plusWeeks(SEMANAS);          // sin incluir

        // 1) Quitar los bloques libres futuros del profesional (los reservados se conservan)
        disponibilidadRepository.deleteByProfesionalIdAndEstadoAndFechaGreaterThanEqual(profesionalId, DISPONIBLE, desde);

        if (servicioProveedorIds == null || servicioProveedorIds.isEmpty() || franjas == null || franjas.isEmpty()) {
            return 0;
        }

        // 2) Lo que quedó (p. ej. RESERVADO) no se vuelve a crear NI se pisa: se guardan sus intervalos por fecha.
        //    HU-26: si cambia la duración, un bloque nuevo puede empezar a otra hora y cruzarse con una cita
        //    ya reservada (de cualquier oferta del profesional); por eso se compara por intervalo, no por hora exacta.
        Map<LocalDate, List<LocalTime[]>> ocupados = new HashMap<>();
        for (Disponibilidad d : disponibilidadRepository.findByProfesionalIdAndFechaGreaterThanEqual(profesionalId, desde)) {
            ocupados.computeIfAbsent(d.getFecha(), f -> new ArrayList<>())
                    .add(new LocalTime[] { d.getHoraInicio(), d.getHoraFin() });
        }

        // 3) Generar: oferta x día x franja, en bloques de la duración de la oferta
        List<Disponibilidad> nuevos = new ArrayList<>();
        for (Long ofertaId : servicioProveedorIds) {
            Integer duracion = consultaOferta.obtenerOferta(ofertaId).map(this::duracionDe).orElse(null);
            if (duracion == null || duracion <= 0) {
                continue;
            }
            for (LocalDate fecha = desde; fecha.isBefore(hasta); fecha = fecha.plusDays(1)) {
                DiaSemana dia = DiaSemana.values()[fecha.getDayOfWeek().getValue() - 1];   // lunes = 0
                for (FranjaHorarioResumen franja : franjas) {
                    if (franja.diaSemana() != dia) {
                        continue;
                    }
                    int inicio = franja.horaInicio().toSecondOfDay() / 60;
                    int fin = franja.horaFin().toSecondOfDay() / 60;
                    // En minutos del día: así nunca "da la vuelta" por la medianoche
                    for (int m = inicio; m + duracion <= fin; m += duracion) {
                        LocalTime horaInicio = LocalTime.ofSecondOfDay(m * 60L);
                        LocalTime horaFin = LocalTime.ofSecondOfDay((m + duracion) * 60L);
                        if (seCruzaConOcupados(ocupados.get(fecha), horaInicio, horaFin)) {
                            continue;
                        }
                        Disponibilidad d = new Disponibilidad();
                        d.setServicioProveedorId(ofertaId);
                        d.setProfesionalId(profesionalId);
                        d.setFecha(fecha);
                        d.setHoraInicio(horaInicio);
                        d.setHoraFin(horaFin);
                        d.setEstado(DISPONIBLE);
                        nuevos.add(d);
                    }
                }
            }
        }
        disponibilidadRepository.saveAll(nuevos);
        return nuevos.size();
    }

    private Integer duracionDe(OfertaDetalle oferta) {
        if (oferta.duracion() != null) {
            return oferta.duracion();
        }
        return (oferta.servicio() == null) ? null : oferta.servicio().duracion();
    }

    /** true si [inicio, fin) se pisa con algún bloque que quedó ese día (tocarse en el borde no es cruce). */
    private static boolean seCruzaConOcupados(List<LocalTime[]> ocupadosDelDia, LocalTime inicio, LocalTime fin) {
        if (ocupadosDelDia == null) {
            return false;
        }
        for (LocalTime[] o : ocupadosDelDia) {
            LocalTime oInicio = o[0];
            LocalTime oFin = o[1];
            if (oInicio == null) {
                continue;
            }
            if (oFin == null) {                                   // bloques antiguos sin hora de fin
                if (oInicio.equals(inicio)) {
                    return true;
                }
                continue;
            }
            if (inicio.isBefore(oFin) && oInicio.isBefore(fin)) {
                return true;
            }
        }
        return false;
    }
}
