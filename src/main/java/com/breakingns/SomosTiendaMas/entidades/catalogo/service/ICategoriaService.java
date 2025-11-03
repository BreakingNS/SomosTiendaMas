package com.breakingns.SomosTiendaMas.entidades.catalogo.service;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.categoria.*;
import java.util.List;

public interface ICategoriaService {
    CategoriaResponseDTO crear(CategoriaCrearDTO dto);
    CategoriaResponseDTO actualizar(Long id, CategoriaActualizarDTO dto);
    CategoriaResponseDTO obtenerPorId(Long id);
    CategoriaResponseDTO obtenerPorSlug(String slug);
    List<CategoriaResumenDTO> listarActivas();
    List<CategoriaArbolDTO> obtenerArbol();
    void eliminar(Long id); // soft-delete
}
