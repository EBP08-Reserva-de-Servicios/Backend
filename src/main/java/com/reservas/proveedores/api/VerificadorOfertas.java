package com.reservas.proveedores.api;

import java.util.List;

/**
 * Pregunta "hacia atrás" de proveedores a catalogo (inversión de dependencias):
 * proveedores declara la interfaz en su api y catalogo la implementa,
 * así proveedores nunca importa catalogo.
 */
public interface VerificadorOfertas {

    /** true si TODAS las ofertas existen y pertenecen al proveedor con ese NIT. */
    boolean pertenecenAlProveedor(Long nit, List<Long> servicioProveedorIds);
}
