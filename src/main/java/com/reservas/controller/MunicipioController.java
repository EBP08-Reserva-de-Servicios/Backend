package com.reservas.controller;

import com.reservas.model.Municipio;
import com.reservas.repository.MunicipioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/municipios")
public class MunicipioController {

    @Autowired
    private MunicipioRepository municipioRepository;

    @GetMapping
    public List<Municipio> listar() {
        return municipioRepository.findAll();
    }

    @PostMapping
    public Municipio crear(@RequestBody Municipio m) {
        return municipioRepository.save(m);
    }
}