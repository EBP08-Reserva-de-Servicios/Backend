package com.reservas.ubicacion.api;

import java.util.Optional;

/**
 * Operaciones que el componente ubicacion ofrece a los demás componentes.
 * Desde fuera de ubicacion solo se usa esta interfaz, nunca el modelo ni el repositorio.
 */
public interface ConsultaUbicaciones {

    Optional<MunicipioResumen> obtenerMunicipio(Long codigoMunicipio);
}