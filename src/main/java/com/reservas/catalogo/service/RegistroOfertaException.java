package com.reservas.catalogo.service;

import java.util.List;

/**
 * Error al publicar o consultar las ofertas de un proveedor (HU-19).
 * El controller traduce: INVALIDO = 400, NO_ENCONTRADO = 404, siempre con {"errores": [...]}.
 */
public class RegistroOfertaException extends RuntimeException {

    public enum Tipo { INVALIDO, NO_ENCONTRADO }

    private final Tipo tipo;
    private final List<String> errores;

    public RegistroOfertaException(Tipo tipo, List<String> errores) {
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
