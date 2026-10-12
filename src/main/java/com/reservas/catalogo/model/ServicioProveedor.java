package com.reservas.catalogo.model;

import jakarta.persistence.*;

@Entity
public class ServicioProveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * NIT del proveedor (componente proveedores).
     * Antes era @ManyToOne Proveedor; ahora es una columna plana con el MISMO nombre.
     */
    @Column(name = "proveedor_nit")
    private Long proveedorNit;

    /** Esta relación SÍ se mantiene: Servicio vive en el mismo componente (catalogo). */
    @ManyToOne
    @JoinColumn(name = "servicio_id")
    private Servicio servicio;

    private Double precio;

    /** HU-19: duración propia de la oferta, en minutos. Integer (no int): las ofertas existentes quedan en NULL. */
    private Integer duracion;

    /** HU-19: descripción propia de la oferta. */
    private String descripcion;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProveedorNit() {
        return proveedorNit;
    }

    public void setProveedorNit(Long proveedorNit) {
        this.proveedorNit = proveedorNit;
    }

    public Servicio getServicio() {
        return servicio;
    }

    public void setServicio(Servicio servicio) {
        this.servicio = servicio;
    }

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    public Integer getDuracion() {
        return duracion;
    }

    public void setDuracion(Integer duracion) {
        this.duracion = duracion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}