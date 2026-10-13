package com.reservas.proveedores.repository;

import com.reservas.proveedores.model.HorarioRecurrente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HorarioRecurrenteRepository extends JpaRepository<HorarioRecurrente, Long> {

    List<HorarioRecurrente> findByProfesionalId(String profesionalId);

    Optional<HorarioRecurrente> findByIdAndProfesionalId(Long id, String profesionalId);
}
