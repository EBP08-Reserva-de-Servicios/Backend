package com.reservas.proveedores.api;

import java.util.List;

/**
 * Vista pública de un profesional (HU-29). Componentes en orden alfabético.
 * Nunca incluye la foto en bytes: solo fotoUrl ("/api/proveedores/{nit}/profesionales/{id}/foto" o null).
 */
public record ProfesionalDetalle(
        String apellidos,
        String correo,
        String fotoUrl,
        String id,
        String nombres,
        String numeroDocumento,
        List<Long> servicioProveedorIds,
        String telefono,
        String tipoDocumento) {
}
