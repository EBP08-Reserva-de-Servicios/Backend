package com.reservas.proveedores.service;

import com.reservas.proveedores.api.CitaDelProfesional;

import java.util.List;

/**
 * Error al gestionar el horario de un profesional (HU-32, HU-21).
 * El controller traduce: INVALIDO = 400, CONFLICTO = 409, NO_ENCONTRADO = 404, con {"errores": [...]}.
 * HU-21: si el cambio deja citas por fuera, el 409 trae además "reservasAfectadas".
 */
public class HorarioException extends RuntimeException {

    public enum Tipo { INVALIDO, CONFLICTO, NO_ENCONTRADO }

    private final Tipo tipo;
    private final List<String> errores;
    private final List<CitaDelProfesional> reservasAfectadas;

    public HorarioException(Tipo tipo, List<String> errores) {
        this(tipo, errores, List.of());
    }

    public HorarioException(Tipo tipo, List<String> errores, List<CitaDelProfesional> reservasAfectadas) {
        super(String.join(" ", errores));
        this.tipo = tipo;
        this.errores = List.copyOf(errores);
        this.reservasAfectadas = List.copyOf(reservasAfectadas);
    }

    public List<CitaDelProfesional> getReservasAfectadas() {
        return reservasAfectadas;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public List<String> getErrores() {
        return errores;
    }
}
