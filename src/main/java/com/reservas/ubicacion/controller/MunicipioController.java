package com.reservas.ubicacion.controller;

import com.reservas.ubicacion.model.Municipio;
import com.reservas.ubicacion.service.MunicipioService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/municipios")
public class MunicipioController {

    private final MunicipioService municipioService;

    public MunicipioController(MunicipioService municipioService) {
        this.municipioService = municipioService;
    }

    @GetMapping
    public List<Municipio> listar() {
        return municipioService.listar();
    }

    @PostMapping
    public Municipio crear(@RequestBody Municipio m) {
        return municipioService.crear(m);
    }
}