package com.reservas.catalogo.controller;

import com.reservas.catalogo.model.Servicio;
import com.reservas.catalogo.model.ServicioProveedor;

/**
 * Cuerpo que recibe POST /api/servicio-proveedor.
 * Conserva la forma de Sprint 1: { "proveedor": { "nit": ... }, "servicio": { "id": ... }, "precio": ... }
 */
public record OfertaSolicitud(
        ProveedorRef proveedor,
        ServicioRef servicio,
        Double precio) {

    public record ProveedorRef(Long nit) {
    }

    public record ServicioRef(Long id) {
    }

    public ServicioProveedor aEntidad() {
        ServicioProveedor sp = new ServicioProveedor();
        sp.setProveedorNit(proveedor == null ? null : proveedor.nit());
        if (servicio != null) {
            // Igual que en Sprint 1: se referencia el servicio solo por su id
            Servicio s = new Servicio();
            s.setId(servicio.id());
            sp.setServicio(s);
        }
        sp.setPrecio(precio);
        return sp;
    }
}