package com.reservas.model;

import jakarta.persistence.*;

@Entity
public class Proveedor {
    @Id
    private Long nit;
    private String razonSocial;
    private String descripcion;
    private String direccion;
    private String telefono;
    private String correo;
    private Double calificacionPromedio;
    private Integer horasLimiteCancelacion;

    @ManyToOne
    @JoinColumn(name = "municipio_id")
    private Municipio municipio;

	public Long getNit() {
		return nit;
	}

	public void setNit(Long nit) {
		this.nit = nit;
	}

	public String getRazonSocial() {
		return razonSocial;
	}

	public void setRazonSocial(String razonSocial) {
		this.razonSocial = razonSocial;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getDireccion() {
		return direccion;
	}

	public void setDireccion(String direccion) {
		this.direccion = direccion;
	}

	public String getTelefono() {
		return telefono;
	}

	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}

	public String getCorreo() {
		return correo;
	}

	public void setCorreo(String correo) {
		this.correo = correo;
	}

	public Double getCalificacionPromedio() {
		return calificacionPromedio;
	}

	public void setCalificacionPromedio(Double calificacionPromedio) {
		this.calificacionPromedio = calificacionPromedio;
	}

	public Integer getHorasLimiteCancelacion() {
		return horasLimiteCancelacion;
	}

	public void setHorasLimiteCancelacion(Integer horasLimiteCancelacion) {
		this.horasLimiteCancelacion = horasLimiteCancelacion;
	}

	public Municipio getMunicipio() {
		return municipio;
	}

	public void setMunicipio(Municipio municipio) {
		this.municipio = municipio;
	}
}