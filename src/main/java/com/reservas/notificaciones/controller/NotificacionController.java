package com.reservas.notificaciones.controller;

import com.reservas.notificaciones.api.NotificacionDetalle;
import com.reservas.notificaciones.service.NotificacionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * HU-36 Ver notificaciones (solo PROVEEDOR; la regla está en SecurityConfig).
 * La empresa sale del token ("sub" = NIT), nunca de la URL ni del cuerpo: nadie ve los avisos de otra empresa.
 *   GET  /api/notificaciones            -> 200 NotificacionDetalle[] (más reciente primero; [] si no hay)
 *   GET  /api/notificaciones/no-leidas  -> 200 {"cantidad": n}
 *   POST /api/notificaciones/{id}/leida -> 204; 404 {"errores": ["Notificación no encontrada."]}
 */
@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    private final NotificacionService notificacionService;

    public NotificacionController(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @GetMapping
    public ResponseEntity<?> listar(@AuthenticationPrincipal Jwt jwt) {
        Long nit = nitDelToken(jwt);
        if (nit == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<NotificacionDetalle> lista = notificacionService.listarDeEmpresa(nit);
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/no-leidas")
    public ResponseEntity<?> noLeidas(@AuthenticationPrincipal Jwt jwt) {
        Long nit = nitDelToken(jwt);
        if (nit == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(Map.of("cantidad", notificacionService.contarNoLeidas(nit)));
    }

    @PostMapping("/{id}/leida")
    public ResponseEntity<?> marcarLeida(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        Long nit = nitDelToken(jwt);
        if (nit == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (!notificacionService.marcarLeida(nit, id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("errores", List.of("Notificación no encontrada.")));
        }
        return ResponseEntity.noContent().build();
    }

    /** NIT de la empresa (claim sub) si el token es de PROVEEDOR; null en cualquier otro caso. */
    private static Long nitDelToken(Jwt jwt) {
        if (jwt == null || !"PROVEEDOR".equals(jwt.getClaimAsString("rol"))) {
            return null;
        }
        try {
            return Long.valueOf(jwt.getSubject());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}