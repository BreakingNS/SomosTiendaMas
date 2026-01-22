package com.breakingns.SomosTiendaMas.entidades.catalogo.service;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto.*;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_centralizado.ProductoCentralizadoResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.enums.CondicionProducto;

import java.util.List;

public interface IProductoService {
    ProductoCentralizadoResponseDTO crear(ProductoCrearDTO dto);
    ProductoCentralizadoResponseDTO crearSoloProducto(ProductoCrearDTO dto); // sin variante default
    ProductoCentralizadoResponseDTO actualizar(Long id, ProductoActualizarDTO dto);
    ProductoCentralizadoResponseDTO obtenerPorId(Long id);
    ProductoCentralizadoResponseDTO obtenerPorSlug(String slug);
    List<ProductoCentralizadoResponseDTO> listarActivas();
    List<ProductoCentralizadoResponseDTO> listarPorCategoriaId(Long categoriaId);
    List<ProductoCentralizadoResponseDTO> listarPorMarcaId(Long marcaId);
    void eliminar(Long id); // soft-delete

    /**
     * Borrado físico permanente. Uso restringido (admins). Endpoint temporal para pruebas.
     */
    void eliminarPermanente(Long id);

    // opcional: listar por condición (nuevo)
    List<ProductoCentralizadoResponseDTO> listarPorCondicion(CondicionProducto condicion);
}