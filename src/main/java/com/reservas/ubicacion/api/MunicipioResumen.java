package com.reservas.ubicacion.api;

/**
 * Vista de solo lectura de un municipio con su departamento.
 * Tiene la misma forma que el JSON de Sprint 1, para poder incrustarla
 * en respuestas de otros componentes sin cambiar lo que recibe el frontend.
 * Componentes en orden alfabético: así el JSON sale en el mismo orden que antes.
 */
public record MunicipioResumen(Long codigo, DepartamentoResumen departamento, String nombre) {
}