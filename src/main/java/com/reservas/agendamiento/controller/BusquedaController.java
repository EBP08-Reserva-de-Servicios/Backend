package com.reservas.agendamiento.controller;

import com.reservas.agendamiento.service.BusquedaService;
import com.reservas.catalogo.api.OfertaDetalle;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/** La URL se conserva tal cual: el frontend no cambia. */
@RestController
@RequestMapping("/api/proveedores")
public class BusquedaController {

    private final BusquedaService busquedaService;

    public BusquedaController(BusquedaService busquedaService) {
        this.busquedaService = busquedaService;
    }

    @GetMapping("/buscar")
    public List<OfertaDetalle> buscar(
            @RequestParam(required = false) Long servicioId,
            @RequestParam(required = false) Long municipioId,
            @RequestParam(required = false) LocalDate fecha,
            @RequestParam(required = false) LocalTime hora) {
        return busquedaService.buscar(servicioId, municipioId, fecha, hora);
    }
}