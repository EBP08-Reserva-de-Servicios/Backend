package com.reservas.notificaciones.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * HU-06: aviso para la EMPRESA (proveedor) cada vez que un cliente agenda una cita.
 * HU-36 las lista y las marca como leídas.
 * El índice único (reserva_codigo, tipo) garantiza en la base UNA notificación por reserva y tipo,
 * aunque el evento llegara dos veces (regla del equipo: nada de check-then-act para unicidad).
 */
@Entity
@Table(name = "notificacion",
        uniqueConstraints = @UniqueConstraint(name = "ux_notificacion_reserva_tipo",
                columnNames = {"reserva_codigo", "tipo"}))
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Empresa que recibe el aviso (id plano del componente proveedores). */
    @Column(name = "proveedor_nit", nullable = false)
    private Long proveedorNit;

    /** Por ahora solo "CITA_AGENDADA". */
    @Column(nullable = false, length = 30)
    private String tipo;

    @Column(nullable = false, length = 500)
    private String mensaje;

    /** Código (UUID) de la reserva que originó el aviso (id plano del componente agendamiento). */
    @Column(name = "reserva_codigo", length = 36)
    private String reservaCodigo;

    @Column(nullable = false)
    private boolean leida;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProveedorNit() { return proveedorNit; }
    public void setProveedorNit(Long proveedorNit) { this.proveedorNit = proveedorNit; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public String getReservaCodigo() { return reservaCodigo; }
    public void setReservaCodigo(String reservaCodigo) { this.reservaCodigo = reservaCodigo; }

    public boolean isLeida() { return leida; }
    public void setLeida(boolean leida) { this.leida = leida; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}