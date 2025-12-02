package com.breakingns.SomosTiendaMas.entidades.catalogo.service;

import org.springframework.web.multipart.MultipartFile;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.imagen.ImagenProductoDTO;

import java.util.List;

public interface IImagenProductoService {
    ImagenProductoDTO crear(ImagenProductoDTO dto);
    ImagenProductoDTO actualizar(Long id, ImagenProductoDTO dto);
    ImagenProductoDTO obtenerPorId(Long id);
    List<ImagenProductoDTO> listarPorProductoId(Long productoId);
    ImagenProductoDTO obtenerPrimeraPorProductoId(Long productoId);
    void eliminar(Long id); // soft-delete
    void eliminarPorProductoId(Long productoId); // soft-delete por producto
    void reordenarPorProducto(Long productoId, List<Long> imagenIdsOrdenados);

    // nuevo: subir archivos, guardarlos y crear las filas correspondientes
    List<ImagenProductoDTO> uploadAndCreate(Long productoId, MultipartFile[] files);
}