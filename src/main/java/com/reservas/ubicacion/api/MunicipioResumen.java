package com.reservas.ubicacion.api;

/**
 * Vista de solo lectura de un municipio con su departamento.
 * Tiene la misma forma que el JSON de Sprint 1, para poder incrustarla
 * en respuestas de otros componentes sin cambiar lo que recibe el frontend.
 */
public record MunicipioResumen(Long codigo, String nombre, DepartamentoResumen departamento) {
}