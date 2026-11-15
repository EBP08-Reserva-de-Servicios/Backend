package com.reservas.clientes.api;

import java.util.Optional;

public interface ConsultaClientes {
    Optional<ClienteResumen> obtenerCliente(String clienteId);

    /** HU-10: perfil completo del cliente (sin clave). Vacío si no existe. */
    Optional<ClientePerfil> obtenerPerfil(String clienteId);
}