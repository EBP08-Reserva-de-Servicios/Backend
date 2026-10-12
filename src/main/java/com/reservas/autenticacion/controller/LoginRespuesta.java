package com.reservas.autenticacion.controller;

import com.reservas.compartido.CuentaResumen;

/** Respuesta 200 del login: {"cuenta": {id, nombres, correo, rol}, "token": "..."}. Nunca incluye la clave. */
public record LoginRespuesta(CuentaResumen cuenta, String token) {
}