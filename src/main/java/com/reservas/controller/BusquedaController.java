package com.reservas.controller;

import com.reservas.model.ServicioProveedor;
import com.reservas.repository.ServicioProveedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/proveedores")
public class BusquedaController {

    @Autowired
    private ServicioProveedorRepository servicioProveedorRepository;

    @GetMapping("/buscar")
    public List<ServicioProveedor> buscar(
            @RequestParam(required = false) Long servicioId,
            @RequestParam(required = false) Long municipioId) {

        List<ServicioProveedor> resultado = (servicioId != null)
                ? servicioProveedorRepository.findByServicioId(servicioId)
                : servicioProveedorRepository.findAll();

        if (municipioId != null) {
            resultado = resultado.stream()
                    .filter(sp -> sp.getProveedor().getMunicipio()
                            .getCodigo().equals(municipioId))
                    .toList();
        }

        return resultado;
    }
}