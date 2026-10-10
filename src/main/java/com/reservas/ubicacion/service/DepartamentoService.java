package com.reservas.ubicacion.service;

import com.reservas.ubicacion.model.Departamento;
import com.reservas.ubicacion.repository.DepartamentoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartamentoService {

    private final DepartamentoRepository departamentoRepository;

    public DepartamentoService(DepartamentoRepository departamentoRepository) {
        this.departamentoRepository = departamentoRepository;
    }

    public List<Departamento> listar() {
        return departamentoRepository.findAll();
    }

    public Departamento crear(Departamento departamento) {
        return departamentoRepository.save(departamento);
    }
}