package com.reservas.controller;

import com.reservas.model.Reserva;
import com.reservas.repository.ReservaRepository;
import com.reservas.repository.DisponibilidadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private DisponibilidadRepository disponibilidadRepository;

    @PostMapping
    public ResponseEntity<?> agendar(@RequestBody Reserva reserva) {
        boolean hayDisponibilidad = !disponibilidadRepository
                .findByServicioProveedorIdAndFechaAndEstado(
                        reserva.getIdServicioProveedor(),
                        reserva.getFecha(),
                        "DISPONIBLE")
                .isEmpty();

        if (!hayDisponibilidad) {
            return ResponseEntity.status(409).body(
                    "La empresa ya no tiene disponibilidad para esa fecha, " +
                    "elige otra opcion.");
        }

        reserva.setEstado("PENDIENTE");
        reserva.setFechaCreacion(LocalDateTime.now());
        Reserva guardada = reservaRepository.save(reserva);
        return ResponseEntity.ok(guardada);
    }

    @GetMapping("/{codigo}")
    public Reserva obtener(@PathVariable String codigo) {
        return reservaRepository.findById(codigo).orElse(null);
    }
}