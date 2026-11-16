package com.reservas.notificaciones.api;

import com.reservas.proveedores.api.ProfesionalResumen;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * HU-36: una notificación tal como la recibe el front (contrato v1.2, sección 6, tipo Notificacion).
 * Componentes en orden alfabético.
 */
public record NotificacionDetalle(
        LocalDateTime fechaCreacion,
        Long id,
        boolean leida,
        String mensaje,
        Reserva reserva,
        String tipo) {

    /** Datos de la cita que originó el aviso. null si la reserva ya no existe. */
    public record Reserva(
            Cliente cliente,
            String codigo,
            String estado,
            LocalDate fecha,
            LocalTime hora,
            LocalTime horaFin,
            ProfesionalResumen profesional,
            String servicio) {
    }

    /** Quién agendó (sin correo ni teléfono: solo lo que pide el contrato). */
    public record Cliente(
            String apellidos,
            String nombres,
            String numeroDocumento) {
    }
}