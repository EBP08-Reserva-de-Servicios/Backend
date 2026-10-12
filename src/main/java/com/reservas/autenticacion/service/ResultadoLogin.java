package com.reservas.autenticacion.service;

import com.reservas.compartido.CuentaResumen;

import java.util.List;

/**
 * Resultado de un intento de login (HU-02, HU-Seg-02).
 * El controller lo traduce a HTTP: INCOMPLETO 400, CREDENCIALES_INVALIDAS 401, BLOQUEADA 423, EXITOSO 200.
 */
public record ResultadoLogin(Tipo tipo, CuentaResumen cuenta, long minutosRestantes, List<String> errores) {

    public enum Tipo { EXITOSO, INCOMPLETO, CREDENCIALES_INVALIDAS, BLOQUEADA }

    public static ResultadoLogin exitoso(CuentaResumen cuenta) {
        return new ResultadoLogin(Tipo.EXITOSO, cuenta, 0, List.of());
    }

    public static ResultadoLogin incompleto(List<String> errores) {
        return new ResultadoLogin(Tipo.INCOMPLETO, null, 0, List.copyOf(errores));
    }

    public static ResultadoLogin credencialesInvalidas() {
        return new ResultadoLogin(Tipo.CREDENCIALES_INVALIDAS, null, 0, List.of());
    }

    public static ResultadoLogin bloqueada(long minutosRestantes) {
        return new ResultadoLogin(Tipo.BLOQUEADA, null, minutosRestantes, List.of());
    }
}