package com.reservas.agendamiento.controller;

import com.reservas.agendamiento.model.Disponibilidad;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Cuerpo que recibe POST /api/disponibilidad.
 * Conserva la forma de Sprint 1: { "servicioProveedor": { "id": ... }, "fecha": ..., ... }
 */
public record DisponibilidadSolicitud(
        ServicioProveedorRef servicioProveedor,
        LocalDate fecha,
        LocalTime horaInicio,
        LocalTime horaFin,
        String estado) {

    public record ServicioProveedorRef(Long id) {
    }

    public Disponibilidad aEntidad() {
        Disponibilidad d = new Disponibilidad();
        d.setServicioProveedorId(servicioProveedor == null ? null : servicioProveedor.id());
        d.setFecha(fecha);
        d.setHoraInicio(horaInicio);
        d.setHoraFin(horaFin);
        d.setEstado(estado);
        return d;
    }
}