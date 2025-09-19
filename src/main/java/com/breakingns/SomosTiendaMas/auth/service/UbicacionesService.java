package com.breakingns.SomosTiendaMas.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.stream.Collectors;

import com.breakingns.SomosTiendaMas.auth.dto.shared.DepartamentoDTO;
import com.breakingns.SomosTiendaMas.auth.dto.shared.LocalidadDTO;
import com.breakingns.SomosTiendaMas.auth.dto.shared.MunicipioDTO;
import com.breakingns.SomosTiendaMas.auth.dto.shared.ProvinciaDTO;
import com.breakingns.SomosTiendaMas.auth.model.Departamento;
import com.breakingns.SomosTiendaMas.auth.model.Localidad;
import com.breakingns.SomosTiendaMas.auth.model.Municipio;
import com.breakingns.SomosTiendaMas.auth.model.Provincia;
import com.breakingns.SomosTiendaMas.auth.repository.IDepartamentoRepository;
import com.breakingns.SomosTiendaMas.auth.repository.ILocalidadRepository;
import com.breakingns.SomosTiendaMas.auth.repository.IMunicipioRepository;
import com.breakingns.SomosTiendaMas.auth.repository.IProvinciaRepository;

@Service
public class UbicacionesService {

    @Autowired
    private IProvinciaRepository provinciaRepository;
    @Autowired
    private IDepartamentoRepository departamentoRepository;
    @Autowired
    private IMunicipioRepository municipioRepository;
    @Autowired
    private ILocalidadRepository localidadRepository;

    public List<Provincia> getProvincias() {
        return provinciaRepository.findAll();
    }

    public List<ProvinciaDTO> getProvinciasDTO() {
        return provinciaRepository.findAll()
            .stream()
            .map(p -> new ProvinciaDTO(p.getId(), p.getNombre()))
            .collect(Collectors.toList());
    }

    public List<Localidad> getLocalidadesPorProvincia(Long provinciaId) {
        return localidadRepository.findByProvinciaId(provinciaId);
    }

    public List<Departamento> getDepartamentosPorProvincia(Long provinciaId) {
        return departamentoRepository.findByProvinciaId(provinciaId);
    }

    public List<DepartamentoDTO> getDepartamentosPorProvinciaDTO(Long provinciaId) {
        return departamentoRepository.findByProvinciaId(provinciaId)
            .stream()
            .map(d -> new DepartamentoDTO(d.getId(), d.getNombre()))
            .collect(Collectors.toList());
    }

    public List<Localidad> getLocalidades(Long provinciaId, Long municipioId) {
        if (municipioId != null) {
            return localidadRepository.findByMunicipioId(municipioId);
        } else if (provinciaId != null) {
            return localidadRepository.findByProvinciaId(provinciaId);
        } else {
            return localidadRepository.findAll();
        }
    }

    public List<LocalidadDTO> getLocalidadesPorMunicipioDTO(Long municipioId) {
        return localidadRepository.findByMunicipioId(municipioId)
            .stream()
            .map(l -> new LocalidadDTO(l.getId(), l.getNombre()))
            .collect(Collectors.toList());
    }

    public List<Municipio> getMunicipioPorLocalida(Long departamentoId) {
        return municipioRepository.findByDepartamentoId(departamentoId);
    }

    public List<MunicipioDTO> getMunicipiosPorDepartamentoDTO(Long departamentoId) {
        List<Municipio> municipios = municipioRepository.findByDepartamentoId(departamentoId);
        return municipios.stream()
            .map(m -> new MunicipioDTO(m.getId(), m.getNombre()))
            .collect(Collectors.toList());
    }

}
