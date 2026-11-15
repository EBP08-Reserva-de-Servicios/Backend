package com.reservas.catalogo.controller;

/**
 * Cuerpo de PUT /api/proveedores/{nit}/servicios/{id} (HU-26).
 * Solo se editan precio, duración y descripción: el servicio del catálogo (nombre y categoría) no cambia.
 */
public record OfertaEditarSolicitud(
        Double precio,
        Integer duracion,
        String descripcion) {
}
