package com.reservas.controller;

import com.reservas.model.ServicioProveedor;
import com.reservas.model.Disponibilidad;
import com.reservas.repository.ServicioProveedorRepository;
import com.reservas.repository.DisponibilidadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/proveedores")
public class BusquedaController {

    @Autowired
    private ServicioProveedorRepository servicioProveedorRepository;

    @Autowired
    private DisponibilidadRepository disponibilidadRepository;

    @GetMapping("/buscar")
    public List<ServicioProveedor> buscar(
            @RequestParam(required = false) Long servicioId,
            @RequestParam(required = false) Long municipioId,
            @RequestParam(required = false) LocalDate fecha,
            @RequestParam(required = false) LocalTime hora) {

        List<ServicioProveedor> resultado = (servicioId != null)
                ? servicioProveedorRepository.findByServicioId(servicioId)
                : servicioProveedorRepository.findAll();

        if (municipioId != null) {
            resultado = resultado.stream()
                    .filter(sp -> sp.getProveedor().getMunicipio()
                            .getCodigo().equals(municipioId))
                    .toList();
        }

        if (fecha != null) {
            resultado = resultado.stream()
                    .filter(sp -> {
                        List<Disponibilidad> disponibles = disponibilidadRepository
                                .findByServicioProveedorIdAndFechaAndEstado(
                                        sp.getId(), fecha, "DISPONIBLE");

                        if (disponibles.isEmpty()) {
                            return false;
                        }

                        if (hora == null) {
                            return true;
                        }

                        LocalTime horaFinCita = hora.plusMinutes(sp.getServicio().getDuracion());

                        return disponibles.stream().anyMatch(d ->
                                !hora.isBefore(d.getHoraInicio())
                                        && !horaFinCita.isAfter(d.getHoraFin()));
                    })
                    .toList();
        }

        return resultado;
    }
}