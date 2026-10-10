package com.reservas.agendamiento.service;

/**
 * La reserva no se puede crear por una regla de negocio
 * (sin disponibilidad, o choque con otra reserva). El controller la traduce a HTTP 409.
 */
public class ConflictoReservaException extends RuntimeException {

    public ConflictoReservaException(String mensaje) {
        super(mensaje);
    }
}