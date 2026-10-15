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
            // Un proveedor registrado sin ubicación (HU-16 en el Sprint 2) no tiene municipio:
            // no coincide con ningún municipio, pero no debe tumbar la búsqueda (antes daba 500).
            resultado = resultado.stream()
                    .filter(o -> estaEnMunicipio(o, municipioId))
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

                        // Duración de la oferta (HU-19); si no tiene, la del servicio; si tampoco, solo la hora de inicio.
                        Integer duracion = duracionDe(o);
                        LocalTime horaFinCita = (duracion == null) ? hora : hora.plusMinutes(duracion);

                        return disponibles.stream().anyMatch(d ->
                                !hora.isBefore(d.getHoraInicio())
                                        && !horaFinCita.isAfter(d.getHoraFin()));
                    })
                    .toList();
        }

        return resultado;
    }

    private static boolean estaEnMunicipio(OfertaDetalle oferta, Long municipioId) {
        if (oferta.proveedor() == null) {
            return false;
        }
        var municipio = oferta.proveedor().municipio();   // sin importar ubicacion: agendamiento no depende de ese componente
        return municipio != null && municipioId.equals(municipio.codigo());
    }

    private static Integer duracionDe(OfertaDetalle oferta) {
        if (oferta.duracion() != null) {
            return oferta.duracion();
        }
        return (oferta.servicio() == null) ? null : oferta.servicio().duracion();
    }
}