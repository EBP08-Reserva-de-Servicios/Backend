package com.reservas.controller;

import com.reservas.catalogo.api.ConsultaOferta;
import com.reservas.catalogo.api.OfertaDetalle;
import com.reservas.model.Disponibilidad;
import com.reservas.repository.DisponibilidadRepository;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Búsqueda de ofertas por servicio, municipio y disponibilidad.
 * Se muda al componente agendamiento en el Paso 6 (decisión: opción A).
 */
@RestController
@RequestMapping("/api/proveedores")
public class BusquedaController {

    private final ConsultaOferta consultaOferta;
    private final DisponibilidadRepository disponibilidadRepository;

    public BusquedaController(ConsultaOferta consultaOferta,
                              DisponibilidadRepository disponibilidadRepository) {
        this.consultaOferta = consultaOferta;
        this.disponibilidadRepository = disponibilidadRepository;
    }

    @GetMapping("/buscar")
    public List<OfertaDetalle> buscar(
            @RequestParam(required = false) Long servicioId,
            @RequestParam(required = false) Long municipioId,
            @RequestParam(required = false) LocalDate fecha,
            @RequestParam(required = false) LocalTime hora) {

        List<OfertaDetalle> resultado = (servicioId != null)
                ? consultaOferta.listarOfertasPorServicio(servicioId)
                : consultaOferta.listarOfertas();

        if (municipioId != null) {
            resultado = resultado.stream()
                    .filter(o -> o.proveedor().municipio().codigo().equals(municipioId))
                    .toList();
        }

        if (fecha != null) {
            resultado = resultado.stream()
                    .filter(o -> {
                        List<Disponibilidad> disponibles = disponibilidadRepository
                                .findByServicioProveedorIdAndFechaAndEstado(
                                        o.id(), fecha, "DISPONIBLE");

                        if (disponibles.isEmpty()) {
                            return false;
                        }

                        if (hora == null) {
                            return true;
                        }

                        LocalTime horaFinCita = hora.plusMinutes(o.servicio().duracion());

                        return disponibles.stream().anyMatch(d ->
                                !hora.isBefore(d.getHoraInicio())
                                        && !horaFinCita.isAfter(d.getHoraFin()));
                    })
                    .toList();
        }

        return resultado;
    }
}