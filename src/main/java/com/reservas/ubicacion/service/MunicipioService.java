package com.reservas.ubicacion.service;

import com.reservas.ubicacion.api.ConsultaUbicaciones;
import com.reservas.ubicacion.api.DepartamentoResumen;
import com.reservas.ubicacion.api.MunicipioResumen;
import com.reservas.ubicacion.model.Departamento;
import com.reservas.ubicacion.model.Municipio;
import com.reservas.ubicacion.repository.MunicipioRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MunicipioService implements ConsultaUbicaciones {

    private final MunicipioRepository municipioRepository;

    public MunicipioService(MunicipioRepository municipioRepository) {
        this.municipioRepository = municipioRepository;
    }

    // ---- Operaciones usadas por MunicipioController (dentro del componente) ----

    public List<Municipio> listar() {
        return municipioRepository.findAll();
    }

    public Municipio crear(Municipio municipio) {
        return municipioRepository.save(municipio);
    }

    // ---- Contrato ConsultaUbicaciones (usado por otros componentes) ----

    @Override
    public Optional<MunicipioResumen> obtenerMunicipio(Long codigoMunicipio) {
        return municipioRepository.findById(codigoMunicipio).map(this::aResumen);
    }

    private MunicipioResumen aResumen(Municipio municipio) {
        Departamento d = municipio.getDepartamento();
        DepartamentoResumen departamento =
                (d == null) ? null : new DepartamentoResumen(d.getCodigo(), d.getNombre());
        return new MunicipioResumen(municipio.getCodigo(), departamento, municipio.getNombre());
    }
}