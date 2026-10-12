package com.reservas.proveedores.controller;

import com.reservas.proveedores.model.Proveedor;

/**
 * Cuerpo que recibe POST /api/proveedores/registro (HU-16).
 * Las claves son las mismas de RegistroProveedorBody en src/api/contratoBackend.ts del prototipo.
 * nit, logo y clave llegan como texto y los convierte/valida ProveedorService.registrar.
 */
public record ProveedorRegistroSolicitud(
        String tipoServicio,
        String razonSocial,
        String logo,                    // data URL: "data:image/png;base64,..."
        String nit,                     // solo dígitos, sin dígito de verificación
        String telefono,
        String correo,
        String representanteNombres,
        String representanteApellidos,
        String representanteCelular,
        String representanteDocumento,
        String clave) {

    /** Arma el proveedor con los campos de texto. NIT, logo y clave se completan en el service. */
    public Proveedor aEntidad() {
        Proveedor p = new Proveedor();
        p.setTipoServicio(tipoServicio);
        p.setRazonSocial(razonSocial);
        p.setTelefono(telefono);
        p.setCorreo(correo);
        p.setRepresentanteNombres(representanteNombres);
        p.setRepresentanteApellidos(representanteApellidos);
        p.setRepresentanteCelular(representanteCelular);
        p.setRepresentanteDocumento(representanteDocumento);
        return p;
    }
}