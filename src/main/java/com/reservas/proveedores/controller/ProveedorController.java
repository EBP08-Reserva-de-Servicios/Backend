package com.reservas.proveedores.controller;

import com.reservas.proveedores.api.ProveedorDetalle;
import com.reservas.proveedores.service.ProveedorService;
import com.reservas.proveedores.service.RegistroProveedorException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    /** HU-16 / HU-Seg-01: registro de proveedor con cuenta (clave cifrada). */
    @PostMapping("/registro")
    public ResponseEntity<?> registrar(@RequestBody ProveedorRegistroSolicitud solicitud) {
        try {
            ProveedorDetalle creado = proveedorService.registrar(
                    solicitud.aEntidad(), solicitud.nit(), solicitud.logo(), solicitud.clave());
            return ResponseEntity.status(HttpStatus.CREATED).body(creado);
        } catch (RegistroProveedorException e) {
            HttpStatus estado = (e.getTipo() == RegistroProveedorException.Tipo.DUPLICADO)
                    ? HttpStatus.CONFLICT        // 409: correo, teléfono, NIT, empresa o logo ya registrados
                    : HttpStatus.BAD_REQUEST;    // 400: datos que no cumplen las reglas
            return ResponseEntity.status(estado).body(Map.of("errores", e.getErrores()));
        }
    }
}