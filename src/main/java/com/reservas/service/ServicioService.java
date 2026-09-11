package com.reservas.service;

import com.reservas.model.Servicio;
import com.reservas.repository.ServicioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServicioService {

    private final ServicioRepository servicioRepository;

    public ServicioService(ServicioRepository servicioRepository) {
        this.servicioRepository = servicioRepository;
    }

    public List<Servicio> obtenerTodos() {
        return servicioRepository.findAll();
    }

    public Servicio guardar(Servicio servicio) {
        return servicioRepository.save(servicio);
    }

    public Servicio obtenerPorId(Long id) {
        return servicioRepository.findById(id).orElse(null);
    }
    public Servicio actualizar(Long id, Servicio servicio) {
    Servicio servicioExistente = servicioRepository.findById(id).orElse(null);

    if (servicioExistente != null) {
        servicioExistente.setNombre(servicio.getNombre());
        servicioExistente.setDescripcion(servicio.getDescripcion());
        servicioExistente.setPrecio(servicio.getPrecio());
        servicioExistente.setDuracion(servicio.getDuracion());

        return servicioRepository.save(servicioExistente);
    }
    return null;
}
public void eliminar(Long id) {
    servicioRepository.deleteById(id);
}
}