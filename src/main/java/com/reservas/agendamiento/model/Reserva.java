package com.reservas.agendamiento.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
public class Reserva {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String codigo;

    private Long idServicioProveedor;
    /** Sprint 1: cliente "de prueba" (número). Desde el contraer (16 oct) ya no se llena: siempre null en reservas nuevas.
     *  La columna se conserva por las reservas antiguas que la tienen (borrarla sería un cambio destructivo). */
    private Long idCliente;
    private LocalDate fecha;
    private LocalTime hora;
    private String estado; // CONFIRMADA o CANCELADA (PENDIENTE solo en reservas antiguas; desde el Sprint 3 nacen CONFIRMADA)
    private LocalDateTime fechaCreacion;

    /** HU-24: UUID del cliente que agendó, tomado del token (nunca del cuerpo). Null en las reservas de Sprint 1. */
    @Column(name = "cliente_id")
    private String clienteId;

    /** HU-24/HU-32: profesional que atiende (el del bloque reservado). Null si el bloque no tenía profesional. */
    @Column(name = "profesional_id")
    private String profesionalId;

    /** HU-26: hora de fin del bloque reservado, guardada al agendar. Así la cita conserva su duración
     *  aunque el proveedor cambie después la duración del servicio. Null en reservas anteriores a este cambio. */
    @Column(name = "hora_fin")
    private LocalTime horaFin;

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public Long getIdServicioProveedor() {
        return idServicioProveedor;
    }

    public void setIdServicioProveedor(Long idServicioProveedor) {
        this.idServicioProveedor = idServicioProveedor;
    }

    public Long getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Long idCliente) {
        this.idCliente = idCliente;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getClienteId() {
        return clienteId;
    }

    public void setClienteId(String clienteId) {
        this.clienteId = clienteId;
    }

    public String getProfesionalId() {
        return profesionalId;
    }

    public void setProfesionalId(String profesionalId) {
        this.profesionalId = profesionalId;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(LocalTime horaFin) {
        this.horaFin = horaFin;
    }
}