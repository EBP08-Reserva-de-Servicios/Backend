package com.reservas.proveedores.api;

import java.time.LocalTime;

/** Franja del horario semanal, para otros componentes (agendamiento genera los bloques con ella). */
public record FranjaHorarioResumen(
        DiaSemana diaSemana,
        LocalTime horaFin,
        LocalTime horaInicio) {
}
