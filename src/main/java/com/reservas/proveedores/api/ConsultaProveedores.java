package com.reservas.proveedores.api;

import java.util.Optional;

/**
 * Operaciones que el componente proveedores ofrece a los demás componentes.
 */
public interface ConsultaProveedores {

    Optional<ProveedorDetalle> obtenerProveedor(Long nit);
}