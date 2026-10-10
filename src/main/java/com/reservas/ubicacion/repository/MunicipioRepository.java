package com.reservas.ubicacion.repository;

import com.reservas.ubicacion.model.Municipio;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MunicipioRepository extends JpaRepository<Municipio, Long> {
    List<Municipio> findByDepartamentoCodigo(Long departamentoId);
}