package com.breakingns.SomosTiendaMas.entidades.catalogo.service;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_centralizado.PhysicalPropertiesDTO;

// REVIEW: se va a utilizar solo IVarianteFisicoService y no IProductoFisicoService, por lo que este controlador queda obsoleto
@Deprecated(since="2026-01-15", forRemoval=true)

public interface IProductoFisicoService {
    PhysicalPropertiesDTO obtenerPorProductoId(Long productoId);
    PhysicalPropertiesDTO crearOActualizarPorProducto(Long productoId, PhysicalPropertiesDTO dto);
    void eliminarPorProductoId(Long productoId);
}