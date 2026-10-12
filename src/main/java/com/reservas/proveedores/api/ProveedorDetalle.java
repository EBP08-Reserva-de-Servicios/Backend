package com.reservas.proveedores.api;

import com.reservas.ubicacion.api.MunicipioResumen;

/**
 * Vista pública de un proveedor, con su municipio anidado.
 * Misma forma que el JSON de Sprint 1 (componentes en orden alfabético) + logoUrl (HU-16).
 * Nunca incluye datos sensibles: ni la clave, ni los bytes del logo, ni los intentos o el bloqueo.
 */
public record ProveedorDetalle(
        Double calificacionPromedio,
        String correo,
        String descripcion,
        String direccion,
        Integer horasLimiteCancelacion,
        String logoUrl,                  // "/api/proveedores/{nit}/logo" o null
        MunicipioResumen municipio,
        Long nit,
        String razonSocial,
        String telefono) {
}