package com.reservas.autenticacion.controller;

import com.reservas.clientes.api.ClientePerfil;
import com.reservas.proveedores.api.ProveedorPerfil;

/**
 * HU-10: respuesta de GET /api/perfil. Según el rol del token llega lleno "cliente" o "proveedor";
 * el otro va en null. Componentes en orden alfabético.
 */
public record PerfilRespuesta(
        ClientePerfil cliente,
        ProveedorPerfil proveedor,
        String rol) {
}