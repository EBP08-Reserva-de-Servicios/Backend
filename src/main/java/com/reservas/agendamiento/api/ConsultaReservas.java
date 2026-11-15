package com.reservas.agendamiento.api;

import java.util.Collection;
import java.util.List;

/** Operaciones de lectura de reservas que agendamiento ofrece a otros componentes (HU-36). */
public interface ConsultaReservas {

    /** Las reservas con esos códigos, en una sola consulta. Los códigos que no existen se omiten. */
    List<ReservaResumen> obtenerReservas(Collection<String> codigos);
}