package com.reservas.agendamiento.api;

import com.reservas.catalogo.api.OfertaDetalle;
import com.reservas.proveedores.api.ProfesionalResumen;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * HU-24 "Mis citas": vista pública de una reserva del cliente, con la oferta y el profesional anidados.
 * Componentes en orden alfabético. profesional es null en reservas sin profesional (Sprint 1).
 * servicioProveedor tiene la misma forma que en DisponibilidadDetalle (el front reutiliza cómo lo pinta).
 */
public record ReservaDetalle(
        String codigo,
        String estado,
        LocalDate fecha,
        LocalTime hora,
        LocalTime horaFin,
        ProfesionalResumen profesional,
        OfertaDetalle servicioProveedor) {
}
