package com.reservas.agendamiento.service;

import com.reservas.agendamiento.model.Disponibilidad;
import com.reservas.catalogo.api.ConsultaOferta;
import com.reservas.catalogo.api.OfertaDetalle;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Búsqueda de ofertas por servicio, municipio y disponibilidad.
 * Vive en agendamiento porque necesita la disponibilidad (decisión: opción A).
 */
@Service
public class BusquedaService {

    private final ConsultaOferta consultaOferta;
    private final DisponibilidadService disponibilidadService;

    public BusquedaService(ConsultaOferta consultaOferta, DisponibilidadService disponibilidadService) {
        this.consultaOferta = consultaOferta;
        this.disponibilidadService = disponibilidadService;
    }

    public List<OfertaDetalle> buscar(Long servicioId, Long municipioId, LocalDate fecha, LocalTime hora) {

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
                        List<Disponibilidad> disponibles =
                                disponibilidadService.listarDisponibles(o.id(), fecha);

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