package com.reservas.agendamiento.api;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * HU-06: evento que agendamiento publica cuando una reserva se guarda (POST /api/reservas exitoso).
 * Lo escucha el componente notificaciones para avisarle a la empresa. Así agendamiento no conoce
 * a notificaciones (la dependencia va de notificaciones hacia este api, nunca al revés).
 * Componentes en orden alfabético.
 */
public record ReservaAgendada(
        String clienteId,
        String codigo,
        LocalDate fecha,
        LocalTime hora,
        LocalTime horaFin,
        Long idServicioProveedor,
        String profesionalId) {
}
