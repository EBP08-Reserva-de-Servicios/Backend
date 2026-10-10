package com.reservas.ubicacion.repository;

import com.reservas.ubicacion.model.Departamento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartamentoRepository extends JpaRepository<Departamento, Long> {
}
