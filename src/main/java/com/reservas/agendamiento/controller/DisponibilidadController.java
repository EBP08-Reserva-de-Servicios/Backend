package com.reservas.agendamiento.controller;

import com.reservas.agendamiento.api.DisponibilidadDetalle;
import com.reservas.agendamiento.service.DisponibilidadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/disponibilidad")
public class DisponibilidadController {

    /** Máximo de días por consulta de rango (el calendario del cliente pide 4 semanas = 29 días). */
    static final int MAX_DIAS_RANGO = 31;

    private final DisponibilidadService disponibilidadService;

    public DisponibilidadController(DisponibilidadService disponibilidadService) {
        this.disponibilidadService = disponibilidadService;
    }

    /**
     * Bloques DISPONIBLES de una oferta.
     * - ?fecha=AAAA-MM-DD              -> un día (forma del Sprint 2, se conserva: expandir sin romper el front).
     * - ?desde=AAAA-MM-DD&hasta=AAAA-MM-DD -> todo el rango en una sola respuesta, ordenado por fecha y hora
     *                                      (mejora de rendimiento: el front pasa de 29 peticiones a 1).
     */
    @GetMapping("/servicio-proveedor/{id}")
    public ResponseEntity<?> consultar(
            @PathVariable Long id,
            @RequestParam(required = false) LocalDate fecha,
            @RequestParam(required = false) LocalDate desde,
            @RequestParam(required = false) LocalDate hasta) {
        if (fecha != null) {
            return ResponseEntity.ok(disponibilidadService.consultarDisponibles(id, fecha));
        }
        if (desde == null || hasta == null) {
            return error("Indica la fecha, o el rango con desde y hasta.");
        }
        if (desde.isAfter(hasta)) {
            return error("La fecha inicial no puede ser posterior a la final.");
        }
        if (ChronoUnit.DAYS.between(desde, hasta) + 1 > MAX_DIAS_RANGO) {
            return error("El rango no puede superar " + MAX_DIAS_RANGO + " días.");
        }
        List<DisponibilidadDetalle> bloques = disponibilidadService.consultarDisponiblesEnRango(id, desde, hasta);
        return ResponseEntity.ok(bloques);
    }

    @PostMapping
    public DisponibilidadDetalle crear(@RequestBody DisponibilidadSolicitud solicitud) {
        return disponibilidadService.crear(solicitud.aEntidad());
    }

    private static ResponseEntity<Map<String, List<String>>> error(String mensaje) {
        return ResponseEntity.badRequest().body(Map.of("errores", List.of(mensaje)));
    }
}