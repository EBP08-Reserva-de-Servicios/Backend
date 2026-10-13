package com.reservas.proveedores.controller;

import com.reservas.proveedores.service.HorarioService;

import java.util.List;

/**
 * Cuerpo de POST /api/proveedores/{nit}/profesionales/{id}/horario (HU-32):
 * { "franjas": [ { "diaSemana": "LUNES", "horaInicio": "08:00", "horaFin": "12:00" } ] }
 */
public record HorarioSolicitud(List<Franja> franjas) {

    public record Franja(String diaSemana, String horaInicio, String horaFin) {
    }

    public List<HorarioService.FranjaTexto> aFranjas() {
        if (franjas == null) {
            return List.of();
        }
        return franjas.stream()
                .map(f -> f == null ? null : new HorarioService.FranjaTexto(f.diaSemana(), f.horaInicio(), f.horaFin()))
                .toList();
    }
}
