package com.reservas.agendamiento.api;

import com.reservas.catalogo.api.OfertaDetalle;
import com.reservas.proveedores.api.ProfesionalResumen;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Vista pública de un bloque de disponibilidad, con la oferta anidada.
 * Componentes en orden alfabético. HU-32 agrega profesional (null en los bloques de Sprint 1).
 */
public record DisponibilidadDetalle(
        String estado,
        LocalDate fecha,
        LocalTime horaFin,
        LocalTime horaInicio,
        Long id,
        ProfesionalResumen profesional,
        OfertaDetalle servicioProveedor) {
}