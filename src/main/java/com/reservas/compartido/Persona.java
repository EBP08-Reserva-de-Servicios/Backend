package com.reservas.compartido;

import jakarta.persistence.MappedSuperclass;

/**
 * Datos comunes a todas las personas del dominio (Cliente, Profesional...).
 * No genera tabla propia: sus campos se agregan a la tabla de cada subclase.
 */
@MappedSuperclass
public abstract class Persona {

    protected String nombres;
    protected String apellidos;
    protected String tipoDocumento;
    protected String numeroDocumento;
    protected String telefono;

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
}