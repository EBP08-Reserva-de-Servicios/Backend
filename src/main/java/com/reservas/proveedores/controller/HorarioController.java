package com.reservas.proveedores.controller;

import com.reservas.proveedores.api.CitaDelProfesional;
import com.reservas.proveedores.api.FranjaHorarioDetalle;
import com.reservas.proveedores.service.HorarioException;
import com.reservas.proveedores.service.HorarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** HU-32: horario semanal de un profesional. HU-21: reemplazar el horario completo (PUT). */
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

    /**
     * HU-21: reemplaza el horario COMPLETO del profesional (todo o nada). 200 con el horario nuevo;
     * 400 datos inválidos; 409 cruce, o citas que quedarían por fuera (con reservasAfectadas); 404 no encontrado.
     */
    @PutMapping
    public ResponseEntity<?> reemplazar(@PathVariable Long nit, @PathVariable String profesionalId,
                                        @RequestBody HorarioSolicitud solicitud) {
        try {
            List<FranjaHorarioDetalle> horario = horarioService.reemplazar(nit, profesionalId, solicitud.aFranjas());
            return ResponseEntity.ok(horario);
        } catch (HorarioException e) {
            return error(e);
        }
    }

    /** 204 sin cuerpo; 404 si la franja no es de ese profesional; 409 (con reservasAfectadas) si tiene citas. */
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

    private ResponseEntity<Map<String, Object>> error(HorarioException e) {
        HttpStatus estado = switch (e.getTipo()) {
            case NO_ENCONTRADO -> HttpStatus.NOT_FOUND;
            case CONFLICTO -> HttpStatus.CONFLICT;
            case INVALIDO -> HttpStatus.BAD_REQUEST;
        };
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("errores", e.getErrores());
        if (!e.getReservasAfectadas().isEmpty()) {
            // Contrato v1.2 (HU-21): [{codigo, fecha, hora}]
            cuerpo.put("reservasAfectadas", e.getReservasAfectadas().stream().map(HorarioController::aAfectada).toList());
        }
        return ResponseEntity.status(estado).body(cuerpo);
    }

    private static Map<String, Object> aAfectada(CitaDelProfesional cita) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("codigo", cita.codigo());
        m.put("fecha", cita.fecha());
        m.put("hora", cita.hora());
        return m;
    }
}
