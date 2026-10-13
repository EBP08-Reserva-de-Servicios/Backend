package com.reservas.proveedores.service;

import com.reservas.proveedores.api.DiaSemana;
import com.reservas.proveedores.api.FranjaHorarioDetalle;
import com.reservas.proveedores.model.HorarioRecurrente;
import com.reservas.proveedores.repository.HorarioRecurrenteRepository;
import com.reservas.proveedores.repository.ProfesionalRepository;
import com.reservas.proveedores.repository.ProveedorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * HU-32: horario semanal (franjas) de un profesional.
 * POST agrega franjas (no reemplaza); GET lista; DELETE quita una franja.
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

    public HorarioService(HorarioRecurrenteRepository horarioRepository,
                          ProfesionalRepository profesionalRepository,
                          ProveedorRepository proveedorRepository) {
        this.horarioRepository = horarioRepository;
        this.profesionalRepository = profesionalRepository;
        this.proveedorRepository = proveedorRepository;
    }

    @Transactional
    public List<FranjaHorarioDetalle> agregar(Long nit, String profesionalId, List<FranjaTexto> franjas) {
        verificarProfesional(nit, profesionalId);

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

        // 2) Cruces: entre las nuevas y contra las ya guardadas del mismo día
        List<HorarioRecurrente> existentes = horarioRepository.findByProfesionalId(profesionalId);
        for (int i = 0; i < nuevas.size(); i++) {
            HorarioRecurrente a = nuevas.get(i);
            boolean cruza = existentes.stream().anyMatch(b -> seCruzan(a, b));
            for (int j = i + 1; j < nuevas.size() && !cruza; j++) {
                cruza = seCruzan(a, nuevas.get(j));
            }
            if (cruza) {
                throw new HorarioException(HorarioException.Tipo.CONFLICTO,
                        List.of("Esta franja se cruza con otra del mismo día"));
            }
        }

        // 3) Guardar y devolver el horario completo
        horarioRepository.saveAll(nuevas);
        return listarDe(profesionalId);
    }

    public List<FranjaHorarioDetalle> listar(Long nit, String profesionalId) {
        verificarProfesional(nit, profesionalId);
        return listarDe(profesionalId);
    }

    @Transactional
    public void eliminar(Long nit, String profesionalId, Long franjaId) {
        verificarProfesional(nit, profesionalId);
        HorarioRecurrente franja = horarioRepository.findByIdAndProfesionalId(franjaId, profesionalId)
                .orElseThrow(() -> new HorarioException(HorarioException.Tipo.NO_ENCONTRADO,
                        List.of("Franja no encontrada.")));
        horarioRepository.delete(franja);
    }

    // ---- Auxiliares ----

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

    private void verificarProfesional(Long nit, String profesionalId) {
        if (nit == null || !proveedorRepository.existsById(nit)) {
            throw new HorarioException(HorarioException.Tipo.NO_ENCONTRADO, List.of("Proveedor no encontrado."));
        }
        if (profesionalId == null || profesionalRepository.findByIdAndProveedorNit(profesionalId, nit).isEmpty()) {
            throw new HorarioException(HorarioException.Tipo.NO_ENCONTRADO, List.of("Profesional no encontrado."));
        }
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
