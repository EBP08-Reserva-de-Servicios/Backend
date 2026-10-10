package com.reservas.ubicacion.api;

/**
 * Vista de solo lectura de un departamento, para otros componentes.
 * Los nombres de campo coinciden con el JSON que ya consume el frontend.
 */
public record DepartamentoResumen(Long codigo, String nombre) {
}