package com.reservas.catalogo.api;

import java.util.List;
import java.util.Optional;

/**
 * Operaciones que el componente catalogo ofrece a los demás componentes.
 */
public interface ConsultaOferta {

    Optional<OfertaDetalle> obtenerOferta(Long servicioProveedorId);

    List<OfertaDetalle> listarOfertas();

    List<OfertaDetalle> listarOfertasPorServicio(Long servicioId);
}