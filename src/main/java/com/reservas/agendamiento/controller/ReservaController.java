package com.reservas.agendamiento.controller;

import com.reservas.agendamiento.model.Reserva;
import com.reservas.agendamiento.service.ConflictoReservaException;
import com.reservas.agendamiento.service.ReservaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @PostMapping
    public ResponseEntity<?> agendar(@RequestBody Reserva reserva) {
        try {
            return ResponseEntity.ok(reservaService.agendar(reserva));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (ConflictoReservaException e) {
            return ResponseEntity.status(409).body(e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @GetMapping("/{codigo}")
    public Reserva obtener(@PathVariable String codigo) {
        // Igual que Sprint 1: si no existe responde 200 con cuerpo vacío (mejora pendiente: 404).
        return reservaService.obtener(codigo).orElse(null);
    }
}