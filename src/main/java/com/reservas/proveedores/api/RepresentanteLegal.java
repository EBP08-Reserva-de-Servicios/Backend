package com.reservas.proveedores.api;

/** HU-10: datos del representante legal de la empresa (dentro de ProveedorPerfil). */
public record RepresentanteLegal(
        String apellidos,
        String celular,
        String documento,
        String nombres) {
}
