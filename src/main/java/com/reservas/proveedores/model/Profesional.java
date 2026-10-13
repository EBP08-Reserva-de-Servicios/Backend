package com.reservas.proveedores.model;

import com.reservas.compartido.Persona;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Profesional de un proveedor (HU-29). Hereda nombres, apellidos, tipoDocumento (opcional),
 * numeroDocumento y telefono de Persona.
 */
@Entity
@Table(name = "profesional")
public class Profesional extends Persona {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true)
    private String correo;

    /** Foto opcional, mismo mecanismo que el logo del proveedor. Sin @Lob: así queda en bytea. */
    private byte[] foto;
    private String fotoTipo;

    /** NIT del proveedor dueño (mismo componente, pero columna plana como en el resto del proyecto). */
    @Column(name = "proveedor_nit")
    private Long proveedorNit;

    /** Ofertas (servicio_proveedor.id) que puede atender. La relación se guarda solo aquí. */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "profesional_servicio_proveedor_ids",
            joinColumns = @JoinColumn(name = "profesional_id"))
    @Column(name = "servicio_proveedor_id")
    private List<Long> servicioProveedorIds = new ArrayList<>();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public byte[] getFoto() { return foto; }
    public void setFoto(byte[] foto) { this.foto = foto; }
    public String getFotoTipo() { return fotoTipo; }
    public void setFotoTipo(String fotoTipo) { this.fotoTipo = fotoTipo; }
    public Long getProveedorNit() { return proveedorNit; }
    public void setProveedorNit(Long proveedorNit) { this.proveedorNit = proveedorNit; }
    public List<Long> getServicioProveedorIds() { return servicioProveedorIds; }
    public void setServicioProveedorIds(List<Long> servicioProveedorIds) { this.servicioProveedorIds = servicioProveedorIds; }
}
