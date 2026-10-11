package com.reservas.clientes.api;

import java.util.Optional;

public interface ConsultaClientes {
    Optional<ClienteResumen> obtenerCliente(String clienteId);
}
