package com.reservas.compartido;

import java.util.Optional;

/**
 * Contrato que cumple cada tipo de cuenta que puede iniciar sesión.
 * Lo implementan ClienteService (clientes) y ProveedorService (proveedores).
 * AuthService (autenticacion) solo conoce esta interfaz, nunca las clases concretas.
 */
public interface CuentaAutenticable {

    /** Rol de este tipo de cuenta: "CLIENTE" o "PROVEEDOR". */
    String rol();

    Optional<CuentaResumen> buscarPorCorreo(String correo);

    boolean validarCredenciales(String correo, String clave);

    boolean estaBloqueada(String correo);

    void registrarIntentoFallido(String correo);

    void registrarIntentoExitoso(String correo);
}