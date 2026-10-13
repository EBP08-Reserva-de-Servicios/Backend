package com.reservas.catalogo.controller;

import com.reservas.catalogo.api.OfertaDetalle;
import com.reservas.catalogo.service.RegistroOfertaException;
import com.reservas.catalogo.service.ServicioProveedorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * HU-19: catálogo de servicios de un proveedor.
 * La URL cuelga de /api/proveedores, pero la oferta pertenece al componente catalogo.
 */
@RestController
@RequestMapping("/api/proveedores/{nit}/servicios")
public class OfertaProveedorController {

    private final ServicioProveedorService servicioProveedorService;

    public OfertaProveedorController(ServicioProveedorService servicioProveedorService) {
        this.servicioProveedorService = servicioProveedorService;
    }

    /** 200 con la lista (puede ser vacía); 404 si el proveedor no existe. */
    @GetMapping
    public ResponseEntity<?> listar(@PathVariable Long nit) {
        try {
            List<OfertaDetalle> ofertas = servicioProveedorService.listarDelProveedor(nit);
            return ResponseEntity.ok(ofertas);
        } catch (RegistroOfertaException e) {
            return error(e);
        }
    }

    /** 201 con la oferta creada; 400 datos inválidos; 404 proveedor inexistente. */
    @PostMapping
    public ResponseEntity<?> publicar(@PathVariable Long nit, @RequestBody OfertaProveedorSolicitud solicitud) {
        try {
            OfertaDetalle creada = servicioProveedorService.publicarDelProveedor(
                    nit, solicitud.aEntidad(), solicitud.servicioId());
            return ResponseEntity.status(HttpStatus.CREATED).body(creada);
        } catch (RegistroOfertaException e) {
            return error(e);
        }
    }

    private ResponseEntity<Map<String, List<String>>> error(RegistroOfertaException e) {
        HttpStatus estado = (e.getTipo() == RegistroOfertaException.Tipo.NO_ENCONTRADO)
                ? HttpStatus.NOT_FOUND
                : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(estado).body(Map.of("errores", e.getErrores()));
    }
}
