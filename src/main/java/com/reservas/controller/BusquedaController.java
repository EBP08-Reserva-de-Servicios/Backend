package com.reservas.controller;

import com.reservas.model.ServicioProveedor;
import com.reservas.repository.ServicioProveedorRepository;
import com.reservas.repository.DisponibilidadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
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
            @RequestParam(required = false) LocalDate fecha) {

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
                    .filter(sp -> !disponibilidadRepository
                            .findByServicioProveedorIdAndFechaAndEstado(
                                    sp.getId(), fecha, "DISPONIBLE")
                            .isEmpty())
                    .toList();
        }

        return resultado;
    }
}