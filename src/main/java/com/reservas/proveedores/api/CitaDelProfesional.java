package com.reservas.proveedores.api;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * HU-21: una cita activa de un profesional, tal como la necesita proveedores para saber si un cambio
 * de horario la deja por fuera. horaFin puede ser null si no se pudo saber (reservas muy antiguas).
 */
public record CitaDelProfesional(String codigo, LocalDate fecha, LocalTime hora, LocalTime horaFin) {
}
