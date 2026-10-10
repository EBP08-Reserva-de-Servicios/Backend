package com.reservas.proveedores.api;

import com.reservas.ubicacion.api.MunicipioResumen;

/**
 * Vista pública de un proveedor, con su municipio anidado.
 * Misma forma que el JSON de Sprint 1 (componentes en orden alfabético).
 * Nunca incluye datos sensibles (en Sprint 2 la clave NO se agrega aquí).
 */
public record ProveedorDetalle(
        Double calificacionPromedio,
        String correo,
        String descripcion,
        String direccion,
        Integer horasLimiteCancelacion,
        MunicipioResumen municipio,
        Long nit,
        String razonSocial,
        String telefono) {
}