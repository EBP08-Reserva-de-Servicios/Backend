package com.reservas.clientes.api;

/**
 * HU-10: perfil del cliente autenticado (lo ve solo él, desde GET /api/perfil).
 * Nunca incluye la clave, los intentos fallidos ni el bloqueo. Componentes en orden alfabético.
 */
public record ClientePerfil(
        String apellidos,
        String correo,
        String id,
        String nombres,
        String numeroDocumento,
        String telefono,
        String tipoDocumento) {
}