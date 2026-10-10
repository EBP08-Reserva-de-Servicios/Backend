package com.reservas.catalogo.controller;

import com.reservas.catalogo.model.ServicioProveedor;
import com.reservas.catalogo.repository.ServicioProveedorRepository;
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