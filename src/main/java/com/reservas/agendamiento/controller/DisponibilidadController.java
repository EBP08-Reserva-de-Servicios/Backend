package com.reservas.agendamiento.controller;

import com.reservas.agendamiento.api.DisponibilidadDetalle;
import com.reservas.agendamiento.service.DisponibilidadService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/disponibilidad")
public class DisponibilidadController {

    private final DisponibilidadService disponibilidadService;

    public DisponibilidadController(DisponibilidadService disponibilidadService) {
        this.disponibilidadService = disponibilidadService;
    }

    @GetMapping("/servicio-proveedor/{id}")
    public List<DisponibilidadDetalle> consultar(
            @PathVariable Long id,
            @RequestParam LocalDate fecha) {
        return disponibilidadService.consultarDisponibles(id, fecha);
    }

    @PostMapping
    public DisponibilidadDetalle crear(@RequestBody DisponibilidadSolicitud solicitud) {
        return disponibilidadService.crear(solicitud.aEntidad());
    }
}