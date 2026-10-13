package com.reservas.proveedores.service;

import java.util.List;

/**
 * Error al registrar o consultar profesionales (HU-29).
 * El controller traduce: INVALIDO = 400, DUPLICADO = 409, NO_ENCONTRADO = 404, con {"errores": [...]}.
 */
public class RegistroProfesionalException extends RuntimeException {

    public enum Tipo { INVALIDO, DUPLICADO, NO_ENCONTRADO }

    private final Tipo tipo;
    private final List<String> errores;

    public RegistroProfesionalException(Tipo tipo, List<String> errores) {
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
