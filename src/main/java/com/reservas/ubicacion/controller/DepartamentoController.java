package com.reservas.ubicacion.controller;

import com.reservas.ubicacion.model.Departamento;
import com.reservas.ubicacion.service.DepartamentoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departamentos")
public class DepartamentoController {

    private final DepartamentoService departamentoService;

    public DepartamentoController(DepartamentoService departamentoService) {
        this.departamentoService = departamentoService;
    }

    @GetMapping
    public List<Departamento> listar() {
        return departamentoService.listar();
    }

    @PostMapping
    public Departamento crear(@RequestBody Departamento d) {
        return departamentoService.crear(d);
    }
}