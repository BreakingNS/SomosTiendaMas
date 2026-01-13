package com.breakingns.SomosTiendaMas.entidades.catalogo.service;

import java.util.List;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.opcion.OpcionValorResponseDTO;

public interface IVarianteValorService {
    OpcionValorResponseDTO asignarValor(Long varianteId, Long valorId);
    void quitarValor(Long varianteId, Long valorId);
    List<OpcionValorResponseDTO> listarValoresPorVarianteId(Long varianteId);
    List<Long> listarVarianteIdsPorValorId(Long valorId);
    boolean existeRelacion(Long varianteId, Long valorId);
    void eliminarRelacion(Long id); // soft-delete
}