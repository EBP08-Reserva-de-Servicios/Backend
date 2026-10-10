package com.reservas.agendamiento.api;

import com.reservas.catalogo.api.OfertaDetalle;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Vista pública de un bloque de disponibilidad, con la oferta anidada.
 * Componentes en orden alfabético: misma forma que el JSON de Sprint 1.
 */
public record DisponibilidadDetalle(
        String estado,
        LocalDate fecha,
        LocalTime horaFin,
        LocalTime horaInicio,
        Long id,
        OfertaDetalle servicioProveedor) {
}