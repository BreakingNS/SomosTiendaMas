package com.breakingns.SomosTiendaMas.entidades.catalogo.service;

import org.springframework.web.multipart.MultipartFile;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.imagen.ImagenVarianteDTO;

import java.util.List;

public interface IImagenVarianteService {
    ImagenVarianteDTO crear(ImagenVarianteDTO dto);
    ImagenVarianteDTO actualizar(Long id, ImagenVarianteDTO dto);
    ImagenVarianteDTO obtenerPorId(Long id);
    List<ImagenVarianteDTO> listarPorVarianteId(Long varianteId);
    ImagenVarianteDTO obtenerPrimeraPorVarianteId(Long varianteId);
    void eliminar(Long id); // soft-delete
    void eliminarPorVarianteId(Long varianteId); // soft-delete por variante
    void reordenarPorVariante(Long varianteId, List<Long> imagenIdsOrdenados);

    // nuevo: subir archivos, guardarlos y crear las filas correspondientes
    List<ImagenVarianteDTO> uploadAndCreate(Long varianteId, MultipartFile[] files);
}