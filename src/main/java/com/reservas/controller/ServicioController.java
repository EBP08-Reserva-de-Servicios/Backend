package com.reservas.controller;

import com.reservas.model.Servicio;
import com.reservas.service.ServicioService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;

@RestController
@RequestMapping("/api/servicios")
public class ServicioController {

    private final ServicioService servicioService;

    public ServicioController(ServicioService servicioService) {
        this.servicioService = servicioService;
    }

    @GetMapping
    public List<Servicio> obtenerTodos() {
        return servicioService.obtenerTodos();
    }

    @GetMapping("/{id}")
    public Servicio obtenerPorId(@PathVariable Long id) {
        return servicioService.obtenerPorId(id);
    }

    @PostMapping
    public Servicio guardar(@RequestBody Servicio servicio) {
        return servicioService.guardar(servicio);
    }
    @PutMapping("/{id}")
public Servicio actualizar(@PathVariable Long id, @RequestBody Servicio servicio) {
    return servicioService.actualizar(id, servicio);
}
@DeleteMapping("/{id}")
public void eliminar(@PathVariable Long id) {
    servicioService.eliminar(id);
}
}