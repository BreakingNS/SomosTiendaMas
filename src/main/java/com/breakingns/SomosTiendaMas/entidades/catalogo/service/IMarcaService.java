package com.breakingns.SomosTiendaMas.entidades.catalogo.service;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.marca.*;
import java.util.List;

public interface IMarcaService {
    MarcaResponseDTO crear(MarcaCrearDTO dto);
    MarcaResponseDTO actualizar(Long id, MarcaActualizarDTO dto);
    MarcaResponseDTO obtenerPorId(Long id);
    MarcaResponseDTO obtenerPorSlug(String slug);
    List<MarcaResumenDTO> listarActivas();
    List<MarcaResumenDTO> listarPorCategoria(Long categoriaId);
    void eliminarLogico(Long id, String usuario);
}