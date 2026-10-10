package com.reservas.catalogo.controller;

import com.reservas.catalogo.api.OfertaDetalle;
import com.reservas.catalogo.service.ServicioProveedorService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/servicio-proveedor")
public class ServicioProveedorController {

    private final ServicioProveedorService servicioProveedorService;

    public ServicioProveedorController(ServicioProveedorService servicioProveedorService) {
        this.servicioProveedorService = servicioProveedorService;
    }

    @PostMapping
    public OfertaDetalle publicar(@RequestBody OfertaSolicitud solicitud) {
        return servicioProveedorService.publicar(solicitud.aEntidad());
    }
}