package com.reservas.proveedores.api;

/**
 * Vista pública de una franja del horario de un profesional (HU-32).
 * Componentes en orden alfabético. Horas en texto "HH:mm" (como las envía y muestra el prototipo).
 */
public record FranjaHorarioDetalle(
        DiaSemana diaSemana,
        String horaFin,
        String horaInicio,
        Long id) {
}
