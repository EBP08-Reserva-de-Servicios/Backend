package com.reservas.proveedores.service;

import java.util.List;

/**
 * Error al gestionar el horario de un profesional (HU-32).
 * El controller traduce: INVALIDO = 400, CONFLICTO = 409, NO_ENCONTRADO = 404, con {"errores": [...]}.
 */
public class HorarioException extends RuntimeException {

    public enum Tipo { INVALIDO, CONFLICTO, NO_ENCONTRADO }

    private final Tipo tipo;
    private final List<String> errores;

    public HorarioException(Tipo tipo, List<String> errores) {
        super(String.join(" ", errores));
        this.tipo = tipo;
        this.errores = List.copyOf(errores);
    }

    public Tipo getTipo() {
        return tipo;
    }

    public List<String> getErrores() {
        return errores;
    }
}
