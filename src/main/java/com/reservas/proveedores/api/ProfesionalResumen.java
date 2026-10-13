package com.reservas.proveedores.api;

/** Vista mínima de un profesional para otros componentes (orden alfabético). */
public record ProfesionalResumen(
        String apellidos,
        String id,
        String nombres) {
}
