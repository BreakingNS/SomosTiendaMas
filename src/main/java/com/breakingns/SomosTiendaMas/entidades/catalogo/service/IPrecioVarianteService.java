package com.breakingns.SomosTiendaMas.entidades.catalogo.service;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.precio.PrecioVarianteCrearDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.precio.PrecioVarianteActualizarDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.precio.PrecioVarianteResponseDTO;
import java.time.LocalDateTime;
import java.util.List;

public interface IPrecioVarianteService {
    PrecioVarianteResponseDTO crear(PrecioVarianteCrearDTO dto);
    PrecioVarianteResponseDTO actualizar(Long id, PrecioVarianteActualizarDTO dto);
    PrecioVarianteResponseDTO obtenerPorId(Long id);
    PrecioVarianteResponseDTO obtenerVigentePorVarianteId(Long varianteId);
    List<PrecioVarianteResponseDTO> listarPorVarianteId(Long varianteId);
    List<PrecioVarianteResponseDTO> buscarVigentesPorVarianteIdEnFecha(Long varianteId, LocalDateTime fecha);
    List<PrecioVarianteResponseDTO> listarActivas();
    void eliminar(Long id); // soft-delete
}