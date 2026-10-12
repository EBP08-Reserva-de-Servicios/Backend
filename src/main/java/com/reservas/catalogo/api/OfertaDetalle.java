package com.reservas.catalogo.api;

import com.reservas.proveedores.api.ProveedorDetalle;

/**
 * Vista pública de una oferta (un servicio publicado por un proveedor, con su precio),
 * con el proveedor y el servicio anidados.
 * Componentes en orden alfabético. HU-19 agrega descripcion y duracion propias de la oferta
 * (null en las ofertas de Sprint 1).
 */
public record OfertaDetalle(
        String descripcion,
        Integer duracion,
        Long id,
        Double precio,
        ProveedorDetalle proveedor,
        ServicioResumen servicio) {
}