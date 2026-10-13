package com.reservas.proveedores.controller;

import com.reservas.proveedores.api.FranjaHorarioDetalle;
import com.reservas.proveedores.service.HorarioException;
import com.reservas.proveedores.service.HorarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** HU-32: horario semanal de un profesional. */
@RestController
@RequestMapping("/api/proveedores/{nit}/profesionales/{profesionalId}/horario")
public class HorarioController {

    private final HorarioService horarioService;

    public HorarioController(HorarioService horarioService) {
        this.horarioService = horarioService;
    }

    /** 200 con las franjas ordenadas por día y hora; 404 si no existe el proveedor o el profesional. */
    @GetMapping
    public ResponseEntity<?> listar(@PathVariable Long nit, @PathVariable String profesionalId) {
        try {
            List<FranjaHorarioDetalle> horario = horarioService.listar(nit, profesionalId);
            return ResponseEntity.ok(horario);
        } catch (HorarioException e) {
            return error(e);
        }
    }

    /** 201 con el horario completo; 400 datos inválidos; 409 cruce; 404 no encontrado. */
    @PostMapping
    public ResponseEntity<?> agregar(@PathVariable Long nit, @PathVariable String profesionalId,
                                     @RequestBody HorarioSolicitud solicitud) {
        try {
            List<FranjaHorarioDetalle> horario = horarioService.agregar(nit, profesionalId, solicitud.aFranjas());
            return ResponseEntity.status(HttpStatus.CREATED).body(horario);
        } catch (HorarioException e) {
            return error(e);
        }
    }

    /** 204 sin cuerpo; 404 si la franja no es de ese profesional. */
    @DeleteMapping("/{franjaId}")
    public ResponseEntity<?> eliminar(@PathVariable Long nit, @PathVariable String profesionalId,
                                      @PathVariable Long franjaId) {
        try {
            horarioService.eliminar(nit, profesionalId, franjaId);
            return ResponseEntity.noContent().build();
        } catch (HorarioException e) {
            return error(e);
        }
    }

    private ResponseEntity<Map<String, List<String>>> error(HorarioException e) {
        HttpStatus estado = switch (e.getTipo()) {
            case NO_ENCONTRADO -> HttpStatus.NOT_FOUND;
            case CONFLICTO -> HttpStatus.CONFLICT;
            case INVALIDO -> HttpStatus.BAD_REQUEST;
        };
        return ResponseEntity.status(estado).body(Map.of("errores", e.getErrores()));
    }
}
