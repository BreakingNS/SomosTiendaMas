package com.breakingns.SomosTiendaMas.entidades.catalogo.service;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.plantilla.*;
import java.util.List;

public interface IPlantillaCategoriaService {
    PlantillaCategoriaResponseDTO crear(PlantillaCategoriaCrearDTO dto);
    PlantillaCategoriaResponseDTO actualizar(Long id, PlantillaCategoriaActualizarDTO dto);
    PlantillaCategoriaResponseDTO obtenerPorId(Long id);
    List<PlantillaCategoriaResumenDTO> listarPorCategoriaId(Long categoriaId);
    List<PlantillaCategoriaResponseDTO> listarActivas();
    void eliminar(Long id); // soft-delete
    PlantillaCategoriaResponseDTO obtenerPorCategoriaIdYNombre(Long categoriaId, String nombre);
}
