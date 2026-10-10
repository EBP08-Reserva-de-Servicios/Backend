package com.reservas.compartido;

/**
 * Datos mínimos de una cuenta, para devolver al login sin exponer la entidad
 * (nunca incluye la clave).
 */
public record CuentaResumen(String id, String nombres, String correo, String rol) {
}