package com.breakingns.SomosTiendaMas.entidades.catalogo.service;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_centralizado.PhysicalPropertiesDTO;

public interface IProductoFisicoService {
    PhysicalPropertiesDTO obtenerPorProductoId(Long productoId);
    PhysicalPropertiesDTO crearOActualizarPorProducto(Long productoId, PhysicalPropertiesDTO dto);
    void eliminarPorProductoId(Long productoId);
}