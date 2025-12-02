package com.breakingns.SomosTiendaMas.entidades.catalogo.service;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.precio.*;
import java.time.LocalDateTime;
import java.util.List;

public interface IPrecioProductoService {
    PrecioProductoResponseDTO crear(PrecioProductoCrearDTO dto);
    PrecioProductoResponseDTO actualizar(Long id, PrecioProductoActualizarDTO dto);
    PrecioProductoResponseDTO obtenerPorId(Long id);
    PrecioProductoResponseDTO obtenerVigentePorProductoId(Long productoId);
    List<PrecioProductoResponseDTO> listarPorProductoId(Long productoId);
    List<PrecioProductoResponseDTO> buscarVigentesPorProductoIdEnFecha(Long productoId, LocalDateTime fecha);
    List<PrecioProductoResponseDTO> listarActivas();
    void eliminar(Long id); // soft-delete
}