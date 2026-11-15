package com.reservas.proveedores.api;

import java.util.Optional;

/**
 * Operaciones que el componente proveedores ofrece a los demás componentes.
 */
public interface ConsultaProveedores {

    Optional<ProveedorDetalle> obtenerProveedor(Long nit);

    /** HU-10: perfil completo del proveedor (con tipo de servicio y representante; sin clave). Vacío si no existe. */
    Optional<ProveedorPerfil> obtenerPerfil(Long nit);
}