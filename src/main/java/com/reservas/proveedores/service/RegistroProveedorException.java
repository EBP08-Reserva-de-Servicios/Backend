package com.reservas.proveedores.service;

import java.util.List;

/**
 * El registro de un proveedor no se puede completar.
 * INVALIDO  -> datos que no cumplen las reglas (el controller responde 400).
 * DUPLICADO -> correo, teléfono, NIT, nombre de empresa o logo ya registrados (el controller responde 409).
 */
public class RegistroProveedorException extends RuntimeException {

    public enum Tipo { INVALIDO, DUPLICADO }

    private final Tipo tipo;
    private final List<String> errores;

    public RegistroProveedorException(Tipo tipo, List<String> errores) {
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