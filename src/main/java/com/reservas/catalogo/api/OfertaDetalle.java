package com.reservas.catalogo.api;

import com.reservas.proveedores.api.ProveedorDetalle;

/**
 * Vista pública de una oferta (un servicio publicado por un proveedor, con su precio),
 * con el proveedor y el servicio anidados. Misma forma que el JSON de Sprint 1.
 */
public record OfertaDetalle(
        Long id,
        Double precio,
        ProveedorDetalle proveedor,
        ServicioResumen servicio) {
}