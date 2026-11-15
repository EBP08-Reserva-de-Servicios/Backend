package com.reservas.proveedores.api;

import com.reservas.ubicacion.api.MunicipioResumen;

/**
 * HU-10: perfil del proveedor autenticado (lo ve solo él, desde GET /api/perfil).
 * A diferencia de ProveedorDetalle (público), incluye el tipo de servicio y el representante legal.
 * Nunca incluye la clave, los bytes del logo, los intentos fallidos ni el bloqueo. Componentes en orden alfabético.
 */
public record ProveedorPerfil(
        String correo,
        String logoUrl,                  // "/api/proveedores/{nit}/logo" o null
        MunicipioResumen municipio,
        Long nit,
        String razonSocial,
        RepresentanteLegal representante,
        String telefono,
        String tipoServicio) {
}
