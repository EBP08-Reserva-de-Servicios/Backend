package com.reservas.controller;

import com.reservas.model.Reserva;
import com.reservas.model.ServicioProveedor;
import com.reservas.repository.ReservaRepository;
import com.reservas.repository.DisponibilidadRepository;
import com.reservas.repository.ServicioProveedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private DisponibilidadRepository disponibilidadRepository;

    @Autowired
    private ServicioProveedorRepository servicioProveedorRepository;

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

        ServicioProveedor sp = servicioProveedorRepository
                .findById(reserva.getIdServicioProveedor())
                .orElse(null);

        if (sp == null) {
            return ResponseEntity.status(404).body("Servicio-proveedor no encontrado.");
        }

        int duracion = sp.getServicio().getDuracion();
        LocalTime horaFinNueva = reserva.getHora().plusMinutes(duracion);

        List<Reserva> reservasDelDia = reservaRepository
                .findByIdServicioProveedorAndFechaAndEstadoNot(
                        reserva.getIdServicioProveedor(),
                        reserva.getFecha(),
                        "CANCELADA");

        boolean hayChoque = reservasDelDia.stream().anyMatch(r -> {
            LocalTime finExistente = r.getHora().plusMinutes(duracion);
            return reserva.getHora().isBefore(finExistente)
                    && horaFinNueva.isAfter(r.getHora());
        });

        if (hayChoque) {
            return ResponseEntity.status(409).body(
                    "Ya existe una reserva en ese horario para esta empresa.");
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