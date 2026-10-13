package com.reservas.agendamiento.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
public class Disponibilidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Id de la oferta (ServicioProveedor, componente catalogo).
     * Antes era @ManyToOne ServicioProveedor; ahora es una columna plana con el MISMO nombre.
     */
    @Column(name = "servicio_proveedor_id")
    private Long servicioProveedorId;

    /** HU-32: profesional que atiende el bloque (componente proveedores). Null en los bloques de Sprint 1. */
    @Column(name = "profesional_id")
    private String profesionalId;

    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String estado; // DISPONIBLE, RESERVADO o BLOQUEADO

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getServicioProveedorId() {
        return servicioProveedorId;
    }

    public void setServicioProveedorId(Long servicioProveedorId) {
        this.servicioProveedorId = servicioProveedorId;
    }

    public String getProfesionalId() {
        return profesionalId;
    }

    public void setProfesionalId(String profesionalId) {
        this.profesionalId = profesionalId;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(LocalTime horaFin) {
        this.horaFin = horaFin;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}