package com.reservas.proveedores.api;

import java.util.Optional;

/** Consultas de profesionales que proveedores ofrece a otros componentes (agendamiento). */
public interface ConsultaProfesionales {

    Optional<ProfesionalResumen> obtenerProfesional(String profesionalId);
}
