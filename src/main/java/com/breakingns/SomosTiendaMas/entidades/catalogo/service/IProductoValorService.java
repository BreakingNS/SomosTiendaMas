package com.breakingns.SomosTiendaMas.entidades.catalogo.service;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.opcion.OpcionValorResponseDTO;
import java.util.List;

public interface IProductoValorService {
    OpcionValorResponseDTO asignarValor(Long productoId, Long valorId);
    void quitarValor(Long productoId, Long valorId);
    List<OpcionValorResponseDTO> listarValoresPorProductoId(Long productoId);
    List<Long> listarProductoIdsPorValorId(Long valorId);
    boolean existeRelacion(Long productoId, Long valorId);
    void eliminarRelacion(Long id); // soft-delete
}
