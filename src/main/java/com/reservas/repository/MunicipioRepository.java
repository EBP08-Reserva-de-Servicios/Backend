package com.reservas.repository;

import com.reservas.model.Municipio;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MunicipioRepository extends JpaRepository<Municipio, Long> {
    List<Municipio> findByDepartamentoCodigo(Long departamentoId);
}