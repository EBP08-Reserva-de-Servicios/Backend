package com.reservas.catalogo.api;

/**
 * Vista pública de un servicio del catálogo general.
 * Componentes en orden alfabético: así el JSON sale igual que en Sprint 1.
 */
public record ServicioResumen(
        String descripcion,
        Integer duracion,
        Long id,
        String nombre,
        Double precio) {
}