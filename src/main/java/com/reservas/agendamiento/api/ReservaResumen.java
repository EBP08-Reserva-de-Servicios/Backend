package com.reservas.agendamiento.api;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * HU-36: datos de una reserva para otros componentes (notificaciones los muestra en cada aviso).
 * horaFin ya viene resuelta: la guardada al agendar o, en reservas antiguas, inicio + duración de la oferta.
 * Componentes en orden alfabético.
 */
public record ReservaResumen(
        String clienteId,
        String codigo,
        String estado,
        LocalDate fecha,
        LocalTime hora,
        LocalTime horaFin,
        Long idServicioProveedor,
        String profesionalId) {
}
