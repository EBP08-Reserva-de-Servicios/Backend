package com.reservas.clientes.controller;

public record ClienteRegistroSolicitud(
    String nombres,
    String apellidos,
    String tipoDocumento,
    String numeroDocumento,
    String telefono,
    String correo,
    String clave
) {}
