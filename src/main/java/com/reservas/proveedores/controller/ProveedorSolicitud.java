package com.reservas.proveedores.controller;

import com.reservas.proveedores.model.Proveedor;

/**
 * Cuerpo que recibe POST /api/proveedores.
 * Conserva la forma de Sprint 1: el municipio llega como objeto { "codigo": ... }.
 */
public record ProveedorSolicitud(
        Long nit,
        String razonSocial,
        String descripcion,
        String direccion,
        String telefono,
        String correo,
        Double calificacionPromedio,
        Integer horasLimiteCancelacion,
        MunicipioRef municipio) {

    /** Solo se lee el código del municipio, igual que hacía JPA con la relación. */
    public record MunicipioRef(Long codigo) {
    }

    public Proveedor aEntidad() {
        Proveedor p = new Proveedor();
        p.setNit(nit);
        p.setRazonSocial(razonSocial);
        p.setDescripcion(descripcion);
        p.setDireccion(direccion);
        p.setTelefono(telefono);
        p.setCorreo(correo);
        p.setCalificacionPromedio(calificacionPromedio);
        p.setHorasLimiteCancelacion(horasLimiteCancelacion);
        p.setMunicipioId(municipio == null ? null : municipio.codigo());
        return p;
    }
}