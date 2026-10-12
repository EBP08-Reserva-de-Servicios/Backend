package com.reservas.autenticacion.controller;

/** Cuerpo de POST /api/auth/login (HU-02). rol: "CLIENTE" o "PROVEEDOR" (selector del prototipo). */
public record LoginSolicitud(String correo, String clave, String rol) {
}
