package com.reservas.proveedores.controller;

import com.reservas.proveedores.model.Profesional;

import java.util.List;

/**
 * Cuerpo de POST /api/proveedores/{nit}/profesionales (HU-29).
 * tipoDocumento y foto son opcionales. foto = data URL base64 ("data:image/png;base64,...") o null.
 */
public record ProfesionalSolicitud(
        String tipoDocumento,
        String numeroDocumento,
        String nombres,
        String apellidos,
        String correo,
        String telefono,
        String foto,
        List<Long> servicioProveedorIds) {

    /** Arma el profesional con los textos; foto, servicios y proveedor los asigna el service. */
    public Profesional aEntidad() {
        Profesional p = new Profesional();
        p.setTipoDocumento(tipoDocumento);
        p.setNumeroDocumento(numeroDocumento);
        p.setNombres(nombres);
        p.setApellidos(apellidos);
        p.setCorreo(correo);
        p.setTelefono(telefono);
        return p;
    }
}
