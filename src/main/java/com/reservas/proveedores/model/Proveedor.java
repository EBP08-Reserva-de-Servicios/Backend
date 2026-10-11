package com.reservas.proveedores.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Proveedor {

    @Id
    private Long nit;
    private String razonSocial;
    private String descripcion;
    private String direccion;
    private String telefono;

    /** Correo de atención al cliente; también es el correo de login (acuerdo 3). */
    @Column(unique = true)
    private String correo;

    private Double calificacionPromedio;
    private Integer horasLimiteCancelacion;

    /**
     * Código del municipio (componente ubicacion).
     * Antes era @ManyToOne Municipio; ahora es una columna plana.
     * El nombre de la columna es el MISMO que usaba el @JoinColumn, así la tabla no cambia.
     */
    @Column(name = "municipio_id")
    private Long municipioId;

    // ---------- HU-16: registro de proveedor ----------

    /** "Tipo de servicios que presta" (texto tal como lo envía el formulario). */
    private String tipoServicio;

    /** Logo de la empresa en bytes (columna bytea). Sin @Lob a propósito. */
    private byte[] logo;

    /** "image/png" o "image/jpeg". */
    private String logoTipo;

    /** SHA-256 del logo, para detectar logos repetidos (HU-16: "no registrado previamente"). */
    private String logoHash;

    private String representanteNombres;
    private String representanteApellidos;
    private String representanteCelular;
    private String representanteDocumento;

    // ---------- HU-Seg-01 / HU-Seg-02: cuenta ----------

    /** Hash BCrypt de la contraseña. Los proveedores precargados la tienen en null. */
    private String clave;

    /** Integer (no int): las filas que ya existen quedan en NULL. */
    private Integer intentosFallidos;

    private LocalDateTime bloqueadoHasta;

    // ---------- getters y setters ----------

    public Long getNit() { return nit; }
    public void setNit(Long nit) { this.nit = nit; }

    public String getRazonSocial() { return razonSocial; }
    public void setRazonSocial(String razonSocial) { this.razonSocial = razonSocial; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public Double getCalificacionPromedio() { return calificacionPromedio; }
    public void setCalificacionPromedio(Double calificacionPromedio) { this.calificacionPromedio = calificacionPromedio; }

    public Integer getHorasLimiteCancelacion() { return horasLimiteCancelacion; }
    public void setHorasLimiteCancelacion(Integer horasLimiteCancelacion) { this.horasLimiteCancelacion = horasLimiteCancelacion; }

    public Long getMunicipioId() { return municipioId; }
    public void setMunicipioId(Long municipioId) { this.municipioId = municipioId; }

    public String getTipoServicio() { return tipoServicio; }
    public void setTipoServicio(String tipoServicio) { this.tipoServicio = tipoServicio; }

    public byte[] getLogo() { return logo; }
    public void setLogo(byte[] logo) { this.logo = logo; }

    public String getLogoTipo() { return logoTipo; }
    public void setLogoTipo(String logoTipo) { this.logoTipo = logoTipo; }

    public String getLogoHash() { return logoHash; }
    public void setLogoHash(String logoHash) { this.logoHash = logoHash; }

    public String getRepresentanteNombres() { return representanteNombres; }
    public void setRepresentanteNombres(String representanteNombres) { this.representanteNombres = representanteNombres; }

    public String getRepresentanteApellidos() { return representanteApellidos; }
    public void setRepresentanteApellidos(String representanteApellidos) { this.representanteApellidos = representanteApellidos; }

    public String getRepresentanteCelular() { return representanteCelular; }
    public void setRepresentanteCelular(String representanteCelular) { this.representanteCelular = representanteCelular; }

    public String getRepresentanteDocumento() { return representanteDocumento; }
    public void setRepresentanteDocumento(String representanteDocumento) { this.representanteDocumento = representanteDocumento; }

    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }

    public Integer getIntentosFallidos() { return intentosFallidos; }
    public void setIntentosFallidos(Integer intentosFallidos) { this.intentosFallidos = intentosFallidos; }

    public LocalDateTime getBloqueadoHasta() { return bloqueadoHasta; }
    public void setBloqueadoHasta(LocalDateTime bloqueadoHasta) { this.bloqueadoHasta = bloqueadoHasta; }
}