package com.reservas.clientes.service;

import java.util.List;

/**
 * El registro de un cliente no se puede completar.
 * INVALIDO  -> datos que no cumplen las reglas (el controller responde 400).
 * DUPLICADO -> correo o documento ya registrados (el controller responde 409).
 */
public class RegistroClienteException extends RuntimeException {

    public enum Tipo { INVALIDO, DUPLICADO }

    private final Tipo tipo;
    private final List<String> errores;

    public RegistroClienteException(Tipo tipo, List<String> errores) {
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