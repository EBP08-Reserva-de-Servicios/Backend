package com.reservas.proveedores.model;

import com.reservas.proveedores.api.DiaSemana;
import jakarta.persistence.*;

import java.time.LocalTime;

/**
 * Franja semanal de atención de un profesional (HU-32): "todos los LUNES de 08:00 a 12:00".
 * No tiene fecha: los bloques con fecha (disponibilidad) se generan a partir de estas reglas.
 */
@Entity
@Table(name = "horario_recurrente")
public class HorarioRecurrente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "profesional_id")
    private String profesionalId;

    @Enumerated(EnumType.STRING)
    private DiaSemana diaSemana;

    private LocalTime horaInicio;

    private LocalTime horaFin;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getProfesionalId() { return profesionalId; }
    public void setProfesionalId(String profesionalId) { this.profesionalId = profesionalId; }
    public DiaSemana getDiaSemana() { return diaSemana; }
    public void setDiaSemana(DiaSemana diaSemana) { this.diaSemana = diaSemana; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }
}
