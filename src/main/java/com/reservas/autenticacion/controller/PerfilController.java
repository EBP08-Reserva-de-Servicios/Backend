package com.reservas.autenticacion.controller;

import com.reservas.clientes.api.ConsultaClientes;
import com.reservas.proveedores.api.ConsultaProveedores;
import com.reservas.proveedores.api.ProveedorPerfil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * HU-10 Ver perfil: GET /api/perfil devuelve los datos de LA CUENTA DEL TOKEN (cliente o proveedor).
 * La identidad sale del token ("sub" = UUID del cliente o NIT del proveedor; "rol"), nunca de la URL:
 * así nadie puede ver el perfil de otro. Sin token, Spring Security responde 401 (regla en SecurityConfig).
 * Vive en autenticacion porque reúne las dos clases de cuenta; solo usa los api de clientes y proveedores.
 */
@RestController
@RequestMapping("/api/perfil")
public class PerfilController {

    private final ConsultaClientes consultaClientes;
    private final ConsultaProveedores consultaProveedores;

    public PerfilController(ConsultaClientes consultaClientes, ConsultaProveedores consultaProveedores) {
        this.consultaClientes = consultaClientes;
        this.consultaProveedores = consultaProveedores;
    }

    /** 200 con el perfil; 403 si el token no es de cliente ni de proveedor; 404 si la cuenta ya no existe. */
    @GetMapping
    public ResponseEntity<?> miPerfil(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {                                   // no debería pasar: la regla exige token
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String rol = jwt.getClaimAsString("rol");
        String sub = jwt.getSubject();

        if ("CLIENTE".equals(rol)) {
            return consultaClientes.obtenerPerfil(sub)
                    .<ResponseEntity<?>>map(c -> ResponseEntity.ok(new PerfilRespuesta(c, null, rol)))
                    .orElseGet(PerfilController::cuentaNoEncontrada);
        }
        if ("PROVEEDOR".equals(rol)) {
            Long nit = leerNit(sub);
            Optional<ProveedorPerfil> perfil = (nit == null) ? Optional.empty() : consultaProveedores.obtenerPerfil(nit);
            return perfil
                    .<ResponseEntity<?>>map(p -> ResponseEntity.ok(new PerfilRespuesta(null, p, rol)))
                    .orElseGet(PerfilController::cuentaNoEncontrada);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    private static ResponseEntity<?> cuentaNoEncontrada() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("errores", List.of("Cuenta no encontrada.")));
    }

    private static Long leerNit(String sub) {
        try {
            return (sub == null) ? null : Long.valueOf(sub);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}