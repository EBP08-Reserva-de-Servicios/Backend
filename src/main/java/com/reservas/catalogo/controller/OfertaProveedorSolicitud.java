package com.reservas.catalogo.controller;

import com.reservas.catalogo.model.ServicioProveedor;

/**
 * Cuerpo de POST /api/proveedores/{nit}/servicios (HU-19).
 * El NIT viene en la URL; el servicio del catálogo, por su id.
 */
public record OfertaProveedorSolicitud(
        Long servicioId,
        Double precio,
        Integer duracion,
        String descripcion) {

    /** Arma la oferta sin proveedor ni servicio: los asigna el service después de validar. */
    public ServicioProveedor aEntidad() {
        ServicioProveedor sp = new ServicioProveedor();
        sp.setPrecio(precio);
        sp.setDuracion(duracion);
        sp.setDescripcion(descripcion);
        return sp;
    }
}
