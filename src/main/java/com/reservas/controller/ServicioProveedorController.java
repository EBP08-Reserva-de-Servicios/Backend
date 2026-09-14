package com.reservas.controller;

import com.reservas.model.ServicioProveedor;
import com.reservas.repository.ServicioProveedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/servicio-proveedor")
public class ServicioProveedorController {

    @Autowired
    private ServicioProveedorRepository servicioProveedorRepository;

    @PostMapping
    public ServicioProveedor publicar(@RequestBody ServicioProveedor sp) {
        return servicioProveedorRepository.save(sp);
    }
}