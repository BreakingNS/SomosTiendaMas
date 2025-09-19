package com.breakingns.SomosTiendaMas.entidades.telefono.service;

import com.breakingns.SomosTiendaMas.entidades.telefono.dto.CodigoAreaDTO;
import com.breakingns.SomosTiendaMas.entidades.telefono.repository.ICodigoAreaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CodigosAreaService {

    @Autowired
    private ICodigoAreaRepository codigoAreaRepository;

    public List<CodigoAreaDTO> listarTodos() {
        return codigoAreaRepository.findAll().stream()
                .map(ca -> new CodigoAreaDTO(ca.getCodigo(), ca.getLocalidad(), ca.getProvincia()))
                .collect(Collectors.toList());
    }

    public Optional<CodigoAreaDTO> buscarPorCodigo(String codigo) {
        return codigoAreaRepository.findByCodigo(codigo)
                .map(ca -> new CodigoAreaDTO(ca.getCodigo(), ca.getLocalidad(), ca.getProvincia()));
    }
}