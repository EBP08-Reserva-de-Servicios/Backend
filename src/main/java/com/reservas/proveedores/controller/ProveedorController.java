package com.reservas.proveedores.controller;

import com.reservas.proveedores.api.ProveedorDetalle;
import com.reservas.proveedores.service.ProveedorService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/proveedores")
public class ProveedorController {

    private final ProveedorService proveedorService;

    public ProveedorController(ProveedorService proveedorService) {
        this.proveedorService = proveedorService;
    }

    @GetMapping
    public List<ProveedorDetalle> listar() {
        return proveedorService.listar();
    }

    @GetMapping("/{nit}")
    public ProveedorDetalle obtener(@PathVariable Long nit) {
        // Igual que Sprint 1: si no existe responde 200 con cuerpo vacío (mejora pendiente: 404).
        return proveedorService.obtener(nit).orElse(null);
    }

    @PostMapping
    public ProveedorDetalle crear(@RequestBody ProveedorSolicitud solicitud) {
        return proveedorService.crear(solicitud.aEntidad());
    }
}