package com.reservas.proveedores.api;

import java.time.LocalDate;
import java.util.List;

/**
 * HU-21: pregunta "hacia atrás" de proveedores a agendamiento (inversión de dependencias, igual que
 * GeneradorDisponibilidad): proveedores declara la interfaz y agendamiento la implementa.
 */
public interface ConsultaCitasDelProfesional {

    /** Citas activas (no canceladas) del profesional con fecha desde "desde", ordenadas por fecha y hora. */
    List<CitaDelProfesional> activasDesde(String profesionalId, LocalDate desde);
}
