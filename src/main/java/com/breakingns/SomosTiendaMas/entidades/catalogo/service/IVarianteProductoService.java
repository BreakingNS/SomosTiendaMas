package com.breakingns.SomosTiendaMas.entidades.catalogo.service;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.VarianteProducto;

import java.util.List;
import java.util.Optional;

public interface IVarianteProductoService {
    VarianteProducto crear(Long productoId, VarianteProducto variante);
    VarianteProducto actualizar(Long id, VarianteProducto cambios);
    Optional<VarianteProducto> obtener(Long id);
    Optional<VarianteProducto> obtenerPorSku(String sku);
    List<VarianteProducto> listarPorProducto(Long productoId);
    void eliminarLogico(Long id, String usuario);

    // Nuevo: asignar valores de opción a una variante
    void asignarValores(Long varianteId, List<Long> valorIds);
}
