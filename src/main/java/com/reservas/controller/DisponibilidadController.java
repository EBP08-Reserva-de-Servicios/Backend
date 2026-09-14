package com.reservas.controller;

import com.reservas.model.Disponibilidad;
import com.reservas.repository.DisponibilidadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/disponibilidad")
public class DisponibilidadController {

    @Autowired
    private DisponibilidadRepository disponibilidadRepository;

    @GetMapping("/servicio-proveedor/{id}")
    public List<Disponibilidad> consultar(
            @PathVariable Long id,
            @RequestParam LocalDate fecha) {
        return disponibilidadRepository
            .findByServicioProveedorIdAndFechaAndEstado(id, fecha, "DISPONIBLE");
    }

    @PostMapping
    public Disponibilidad crear(@RequestBody Disponibilidad d) {
        return disponibilidadRepository.save(d);
    }
}