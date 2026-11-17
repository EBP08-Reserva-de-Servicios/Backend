package com.reservas.proveedores.service;

import com.reservas.proveedores.api.CitaDelProfesional;
import com.reservas.proveedores.api.ConsultaCitasDelProfesional;
import com.reservas.proveedores.api.DiaSemana;
import com.reservas.proveedores.api.DuracionOfertaModificada;
import com.reservas.proveedores.api.FranjaHorarioDetalle;
import com.reservas.proveedores.api.FranjaHorarioResumen;
import com.reservas.proveedores.api.GeneradorDisponibilidad;
import com.reservas.proveedores.model.HorarioRecurrente;
import com.reservas.proveedores.model.Profesional;
import com.reservas.proveedores.repository.HorarioRecurrenteRepository;
import com.reservas.proveedores.repository.ProfesionalRepository;
import com.reservas.proveedores.repository.ProveedorRepository;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * HU-32: horario semanal (franjas) de un profesional.
 * POST agrega franjas (no reemplaza); GET lista; DELETE quita una franja.
 * HU-21: PUT reemplaza el horario completo (todo o nada). Ningún cambio puede dejar citas activas por fuera
 * (409 con reservasAfectadas) y los cambios del mismo profesional se atienden de a uno (bloqueo de su fila).
 * Después de cada cambio se regeneran los bloques de disponibilidad del profesional
 * (GeneradorDisponibilidad, implementado en agendamiento: inversión de dependencias).
 */
@Service
public class HorarioService {

    private static final DateTimeFormatter LEER_HORA = DateTimeFormatter.ofPattern("H:mm");
    private static final DateTimeFormatter MOSTRAR_HORA = DateTimeFormatter.ofPattern("HH:mm");

    /** Una franja tal como llega en el cuerpo (texto), antes de validarla. */
    public record FranjaTexto(String diaSemana, String horaInicio, String horaFin) {
    }

    private final HorarioRecurrenteRepository horarioRepository;
    private final ProfesionalRepository profesionalRepository;
    private final ProveedorRepository proveedorRepository;
    private final GeneradorDisponibilidad generadorDisponibilidad;     // lo implementa agendamiento
    private final ConsultaCitasDelProfesional consultaCitas;           // HU-21: también la implementa agendamiento

    private static final String MENSAJE_CRUCE = "Esta franja se cruza con otra del mismo día";
    private static final String MENSAJE_CITAS =
            "Hay reservas en este horario. Reprograma o cancela las reservas antes de modificarlo";

    public HorarioService(HorarioRecurrenteRepository horarioRepository,
                          ProfesionalRepository profesionalRepository,
                          ProveedorRepository proveedorRepository,
                          GeneradorDisponibilidad generadorDisponibilidad,
                          ConsultaCitasDelProfesional consultaCitas) {
        this.horarioRepository = horarioRepository;
        this.profesionalRepository = profesionalRepository;
        this.proveedorRepository = proveedorRepository;
        this.generadorDisponibilidad = generadorDisponibilidad;
        this.consultaCitas = consultaCitas;
    }

    @Transactional
    public List<FranjaHorarioDetalle> agregar(Long nit, String profesionalId, List<FranjaTexto> franjas) {
        Profesional profesional = verificarProfesional(nit, profesionalId, true);
        List<HorarioRecurrente> nuevas = leerFranjas(profesionalId, franjas);

        // Cruces: entre las nuevas y contra las ya guardadas del mismo día
        verificarCruces(nuevas, horarioRepository.findByProfesionalId(profesionalId));

        // Guardar, regenerar los bloques y devolver el horario completo
        guardar(nuevas);
        regenerarDisponibilidad(profesional);
        return listarDe(profesionalId);
    }

    /**
     * HU-21: reemplaza el horario COMPLETO del profesional en una sola transacción: o se guarda todo o nada.
     * Valida formato y cruces (como el POST) y que ninguna cita activa quede por fuera del horario nuevo.
     */
    @Transactional
    public List<FranjaHorarioDetalle> reemplazar(Long nit, String profesionalId, List<FranjaTexto> franjas) {
        Profesional profesional = verificarProfesional(nit, profesionalId, true);
        List<HorarioRecurrente> nuevas = leerFranjas(profesionalId, franjas);
        verificarCruces(nuevas, List.of());
        verificarCitas(profesionalId, nuevas);

        // Primero se borran las franjas anteriores (flush) y luego se insertan las nuevas,
        // así el índice único ux_horario_franja_unica no choca con una franja que se conserva igual.
        horarioRepository.deleteAll(horarioRepository.findByProfesionalId(profesionalId));
        horarioRepository.flush();
        guardar(nuevas);
        regenerarDisponibilidad(profesional);
        return listarDe(profesionalId);
    }

    public List<FranjaHorarioDetalle> listar(Long nit, String profesionalId) {
        verificarProfesional(nit, profesionalId, false);
        return listarDe(profesionalId);
    }

    @Transactional
    public void eliminar(Long nit, String profesionalId, Long franjaId) {
        Profesional profesional = verificarProfesional(nit, profesionalId, true);
        HorarioRecurrente franja = horarioRepository.findByIdAndProfesionalId(franjaId, profesionalId)
                .orElseThrow(() -> new HorarioException(HorarioException.Tipo.NO_ENCONTRADO,
                        List.of("Franja no encontrada.")));
        // HU-21: no se puede quitar una franja que tiene citas activas
        List<HorarioRecurrente> restantes = horarioRepository.findByProfesionalId(profesionalId).stream()
                .filter(h -> !h.getId().equals(franja.getId()))
                .toList();
        verificarCitas(profesionalId, restantes);
        horarioRepository.delete(franja);
        regenerarDisponibilidad(profesional);
    }

    /** Lee y valida las franjas del cuerpo (400 con los mensajes sin repetir). */
    private List<HorarioRecurrente> leerFranjas(String profesionalId, List<FranjaTexto> franjas) {
        if (franjas == null || franjas.isEmpty()) {
            throw new HorarioException(HorarioException.Tipo.INVALIDO, List.of("Selecciona al menos un día"));
        }

        // 1) Formato y orden de cada franja (mensajes sin repetir)
        Set<String> errores = new LinkedHashSet<>();
        List<HorarioRecurrente> nuevas = new ArrayList<>();
        for (FranjaTexto f : franjas) {
            if (f == null) {
                errores.add("Selecciona al menos un día");
                continue;
            }
            DiaSemana dia = leerDia(f.diaSemana());
            LocalTime inicio = leerHora(f.horaInicio());
            LocalTime fin = leerHora(f.horaFin());
            if (dia == null) {
                errores.add("El día de la semana no es válido.");
            }
            if (inicio == null || fin == null) {
                errores.add("La hora debe tener el formato HH:mm.");
            } else if (!fin.isAfter(inicio)) {
                errores.add("La hora de fin debe ser posterior a la de inicio");
            }
            if (dia != null && inicio != null && fin != null && fin.isAfter(inicio)) {
                HorarioRecurrente h = new HorarioRecurrente();
                h.setProfesionalId(profesionalId);
                h.setDiaSemana(dia);
                h.setHoraInicio(inicio);
                h.setHoraFin(fin);
                nuevas.add(h);
            }
        }
        if (!errores.isEmpty()) {
            throw new HorarioException(HorarioException.Tipo.INVALIDO, new ArrayList<>(errores));
        }
        return nuevas;
    }

    /** 409 si alguna franja nueva se cruza con otra nueva o con una de "existentes" del mismo día. */
    private static void verificarCruces(List<HorarioRecurrente> nuevas, List<HorarioRecurrente> existentes) {
        for (int i = 0; i < nuevas.size(); i++) {
            HorarioRecurrente a = nuevas.get(i);
            boolean cruza = existentes.stream().anyMatch(b -> seCruzan(a, b));
            for (int j = i + 1; j < nuevas.size() && !cruza; j++) {
                cruza = seCruzan(a, nuevas.get(j));
            }
            if (cruza) {
                throw new HorarioException(HorarioException.Tipo.CONFLICTO, List.of(MENSAJE_CRUCE));
            }
        }
    }

    /**
     * HU-21: 409 con reservasAfectadas si alguna cita activa (de hoy en adelante) no queda completa dentro
     * de alguna franja de su día en el horario que quedaría. No se guarda nada.
     */
    private void verificarCitas(String profesionalId, List<HorarioRecurrente> horarioQueQueda) {
        List<CitaDelProfesional> afectadas = consultaCitas.activasDesde(profesionalId, LocalDate.now()).stream()
                .filter(cita -> horarioQueQueda.stream().noneMatch(f -> contiene(f, cita)))
                .toList();
        if (!afectadas.isEmpty()) {
            throw new HorarioException(HorarioException.Tipo.CONFLICTO, List.of(MENSAJE_CITAS), afectadas);
        }
    }

    /** La franja es del mismo día de la semana y la cita cabe completa: inicio <= hora y horaFin <= fin. */
    private static boolean contiene(HorarioRecurrente franja, CitaDelProfesional cita) {
        if (cita.fecha() == null || cita.hora() == null) {
            return true;                                   // datos incompletos: no bloquear el cambio por ella
        }
        DiaSemana dia = DiaSemana.values()[cita.fecha().getDayOfWeek().getValue() - 1];   // lunes = 0
        LocalTime fin = (cita.horaFin() != null) ? cita.horaFin() : cita.hora();
        return franja.getDiaSemana() == dia
                && !cita.hora().isBefore(franja.getHoraInicio())
                && !fin.isAfter(franja.getHoraFin());
    }

    /** Guarda las franjas. Si aun así choca con el índice único (ux_horario_franja_unica), responde 409, no 500. */
    private void guardar(List<HorarioRecurrente> nuevas) {
        try {
            horarioRepository.saveAllAndFlush(nuevas);
        } catch (DataIntegrityViolationException e) {
            throw new HorarioException(HorarioException.Tipo.CONFLICTO, List.of(MENSAJE_CRUCE));
        }
    }

    /**
     * Vuelve a generar los bloques de las próximas semanas para TODOS los profesionales con horario.
     * La usa ExtensionDisponibilidadTarea (al arrancar y cada día), para que el calendario no se quede
     * vacío cuando pasan las 4 semanas (observación de Arelis). Devuelve cuántos bloques se crearon.
     */
    @Transactional
    public int extenderDisponibilidad() {
        Set<String> conHorario = new TreeSet<>();
        horarioRepository.findAll().forEach(h -> conHorario.add(h.getProfesionalId()));
        int total = 0;
        for (String profesionalId : conHorario) {
            Profesional profesional = profesionalRepository.findById(profesionalId).orElse(null);
            if (profesional != null) {
                total += regenerarDisponibilidad(profesional);
            }
        }
        return total;
    }

    /**
     * HU-26: un proveedor cambió la duración de una oferta (evento que publica catalogo).
     * Se regeneran los bloques libres de cada profesional que la atiende, ya con la nueva duración.
     * Corre en la misma transacción de la edición: si algo falla, la edición tampoco se guarda.
     */
    @EventListener
    @Transactional
    public void alCambiarDuracionDeOferta(DuracionOfertaModificada evento) {
        for (Profesional profesional : profesionalRepository.buscarPorOferta(evento.servicioProveedorId())) {
            regenerarDisponibilidad(profesional);
        }
    }

    /**
     * HU-20: cambiaron los servicios que presta el profesional. Se regeneran sus bloques libres con los
     * servicios nuevos; los bloques reservados (las citas) se conservan. Corre en la transacción de la edición.
     */
    @Transactional
    public void alCambiarServiciosDelProfesional(Profesional profesional) {
        regenerarDisponibilidad(profesional);
    }

    // ---- Auxiliares ----

    /** Vuelve a generar los bloques futuros del profesional con su horario actual. */
    private int regenerarDisponibilidad(Profesional profesional) {
        List<FranjaHorarioResumen> franjas = horarioRepository.findByProfesionalId(profesional.getId()).stream()
                .map(h -> new FranjaHorarioResumen(h.getDiaSemana(), h.getHoraFin(), h.getHoraInicio()))
                .toList();
        return generadorDisponibilidad.regenerar(profesional.getId(), profesional.getServicioProveedorIds(), franjas);
    }

    private List<FranjaHorarioDetalle> listarDe(String profesionalId) {
        return horarioRepository.findByProfesionalId(profesionalId).stream()
                .sorted(Comparator.comparing((HorarioRecurrente h) -> h.getDiaSemana().ordinal())
                        .thenComparing(HorarioRecurrente::getHoraInicio))
                .map(h -> new FranjaHorarioDetalle(
                        h.getDiaSemana(),
                        h.getHoraFin().format(MOSTRAR_HORA),
                        h.getHoraInicio().format(MOSTRAR_HORA),
                        h.getId()))
                .toList();
    }

    /** bloquear = true en los cambios (POST, PUT, DELETE): bloquea la fila del profesional hasta el commit. */
    private Profesional verificarProfesional(Long nit, String profesionalId, boolean bloquear) {
        if (nit == null || !proveedorRepository.existsById(nit)) {
            throw new HorarioException(HorarioException.Tipo.NO_ENCONTRADO, List.of("Proveedor no encontrado."));
        }
        if (profesionalId == null) {
            throw new HorarioException(HorarioException.Tipo.NO_ENCONTRADO, List.of("Profesional no encontrado."));
        }
        return (bloquear
                ? profesionalRepository.bloquearPorIdYProveedor(profesionalId, nit)
                : profesionalRepository.findByIdAndProveedorNit(profesionalId, nit))
                .orElseThrow(() -> new HorarioException(HorarioException.Tipo.NO_ENCONTRADO,
                        List.of("Profesional no encontrado.")));
    }

    /** Mismo día y se pisan: inicioA < finB y inicioB < finA (tocarse en el borde no es cruce). */
    private static boolean seCruzan(HorarioRecurrente a, HorarioRecurrente b) {
        return a.getDiaSemana() == b.getDiaSemana()
                && a.getHoraInicio().isBefore(b.getHoraFin())
                && b.getHoraInicio().isBefore(a.getHoraFin());
    }

    /** Acepta "LUNES", "lunes", "MIÉRCOLES" o "Sábado": quita tildes y pasa a mayúsculas. */
    private static DiaSemana leerDia(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        String limpio = Normalizer.normalize(texto.trim().toUpperCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        try {
            return DiaSemana.valueOf(limpio);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /** Acepta "08:00" y "8:00". */
    private static LocalTime leerHora(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        try {
            return LocalTime.parse(texto.trim(), LEER_HORA);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
