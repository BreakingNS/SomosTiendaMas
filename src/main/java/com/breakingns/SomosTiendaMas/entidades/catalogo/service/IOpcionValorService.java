package com.breakingns.SomosTiendaMas.entidades.catalogo.service;

import java.util.List;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.opcion.*;

public interface IOpcionValorService {
    OpcionValorResponseDTO crear(OpcionValorCrearDTO dto);
    OpcionValorResponseDTO actualizar(Long id, OpcionValorActualizarDTO dto);
    OpcionValorResponseDTO obtenerPorId(Long id);
    OpcionValorResponseDTO obtenerPorSlug(String slug);
    List<OpcionValorResponseDTO> listarPorOpcionId(Long opcionId);
    List<OpcionValorResponseDTO> listarActivos();
    boolean existeValorEnOpcion(Long opcionId, String valor);
    void eliminar(Long id); // soft-delete
    void eliminarPorOpcionId(Long opcionId); // borrado en bloque
}