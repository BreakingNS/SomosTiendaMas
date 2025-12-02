package com.breakingns.SomosTiendaMas.entidades.catalogo.service;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto.*;
import com.breakingns.SomosTiendaMas.entidades.catalogo.enums.CondicionProducto;

import java.util.List;

public interface IProductoService {
    ProductoResponseDTO crear(ProductoCrearDTO dto);
    ProductoResponseDTO actualizar(Long id, ProductoActualizarDTO dto);
    ProductoResponseDTO obtenerPorId(Long id);
    ProductoResponseDTO obtenerPorSlug(String slug);
    List<ProductoResponseDTO> listarActivas();
    List<ProductoResponseDTO> listarPorCategoriaId(Long categoriaId);
    List<ProductoResponseDTO> listarPorMarcaId(Long marcaId);
    void eliminar(Long id); // soft-delete

    // opcional: listar por condición (nuevo)
    List<ProductoResponseDTO> listarPorCondicion(CondicionProducto condicion);
}
