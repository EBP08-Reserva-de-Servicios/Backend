package com.reservas.agendamiento.controller;

import com.reservas.agendamiento.model.Reserva;
import com.reservas.agendamiento.service.ConflictoReservaException;
import com.reservas.agendamiento.service.ReservaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

/**
 * Reservas. El cliente SIEMPRE sale del token (claim sub), nunca del cuerpo ni de la URL (evita IDOR).
 * jwt llega null si no hay token o si el backend aún no verifica tokens (antes de A3a).
 */
@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    /** Errores en TEXTO PLANO, como en Sprint 1 (el front actual los muestra así): 400, 404 o 409. */
    @PostMapping
    public ResponseEntity<?> agendar(@RequestBody Reserva reserva, @AuthenticationPrincipal Jwt jwt) {
        try {
            return ResponseEntity.ok(reservaService.agendar(reserva, clienteDelToken(jwt)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (ConflictoReservaException e) {
            return ResponseEntity.status(409).body(e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    /** HU-24 "Mis citas": 200 con la lista (puede ser []), 401 sin token, 403 si el token no es de cliente. */
    @GetMapping("/mias")
    public ResponseEntity<?> mias(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ResponseEntity.status(401).body("Inicia sesión para ver tus citas.");
        }
        String clienteId = clienteDelToken(jwt);
        if (clienteId == null) {
            return ResponseEntity.status(403).body("Solo los clientes tienen citas.");
        }
        return ResponseEntity.ok(reservaService.listarDelCliente(clienteId));
    }

    @GetMapping("/{codigo}")
    public Reserva obtener(@PathVariable String codigo) {
        // Igual que Sprint 1: si no existe responde 200 con cuerpo vacío (mejora pendiente: 404).
        return reservaService.obtener(codigo).orElse(null);
    }

    /** UUID del cliente (claim sub) si el token es de rol CLIENTE; null en cualquier otro caso. */
    private static String clienteDelToken(Jwt jwt) {
        return (jwt != null && "CLIENTE".equals(jwt.getClaimAsString("rol"))) ? jwt.getSubject() : null;
    }
}