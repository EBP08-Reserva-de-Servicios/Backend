package com.reservas.controller;

import com.reservas.model.Departamento;
import com.reservas.repository.DepartamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/departamentos")
public class DepartamentoController {

    @Autowired
    private DepartamentoRepository departamentoRepository;

    @GetMapping
    public List<Departamento> listar() {
        return departamentoRepository.findAll();
    }

    @PostMapping
    public Departamento crear(@RequestBody Departamento d) {
        return departamentoRepository.save(d);
    }
}