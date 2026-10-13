package com.reservas.proveedores.controller;

import com.reservas.proveedores.api.ProfesionalDetalle;
import com.reservas.proveedores.model.Profesional;
import com.reservas.proveedores.service.ProfesionalService;
import com.reservas.proveedores.service.RegistroProfesionalException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** HU-29: profesionales de un proveedor. */
@RestController
@RequestMapping("/api/proveedores/{nit}/profesionales")
public class ProfesionalController {

    private final ProfesionalService profesionalService;

    public ProfesionalController(ProfesionalService profesionalService) {
        this.profesionalService = profesionalService;
    }

    /** 200 con la lista (puede ser vacía); 404 si el proveedor no existe. */
    @GetMapping
    public ResponseEntity<?> listar(@PathVariable Long nit) {
        try {
            List<ProfesionalDetalle> profesionales = profesionalService.listar(nit);
            return ResponseEntity.ok(profesionales);
        } catch (RegistroProfesionalException e) {
            return error(e);
        }
    }

    /** 201 creado; 400 datos inválidos; 409 duplicado; 404 proveedor inexistente. */
    @PostMapping
    public ResponseEntity<?> registrar(@PathVariable Long nit, @RequestBody ProfesionalSolicitud solicitud) {
        try {
            ProfesionalDetalle creado = profesionalService.registrar(
                    nit, solicitud.aEntidad(), solicitud.foto(), solicitud.servicioProveedorIds());
            return ResponseEntity.status(HttpStatus.CREATED).body(creado);
        } catch (RegistroProfesionalException e) {
            return error(e);
        }
    }

    /** Imagen de la foto del profesional. 404 si no tiene foto. */
    @GetMapping("/{id}/foto")
    public ResponseEntity<byte[]> foto(@PathVariable Long nit, @PathVariable String id) {
        return profesionalService.obtenerConFoto(nit, id)
                .map((Profesional p) -> ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(p.getFotoTipo()))
                        .body(p.getFoto()))
                .orElse(ResponseEntity.notFound().build());
    }

    private ResponseEntity<Map<String, List<String>>> error(RegistroProfesionalException e) {
        HttpStatus estado = switch (e.getTipo()) {
            case NO_ENCONTRADO -> HttpStatus.NOT_FOUND;
            case DUPLICADO -> HttpStatus.CONFLICT;
            case INVALIDO -> HttpStatus.BAD_REQUEST;
        };
        return ResponseEntity.status(estado).body(Map.of("errores", e.getErrores()));
    }
}
