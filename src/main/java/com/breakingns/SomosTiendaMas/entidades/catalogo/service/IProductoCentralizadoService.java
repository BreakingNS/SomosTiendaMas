package com.breakingns.SomosTiendaMas.entidades.catalogo.service;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_centralizado.ProductCreateDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_centralizado.ProductoDetalleResponseDTO;

public interface IProductoCentralizadoService {
    ProductoDetalleResponseDTO crear(ProductCreateDTO dto);
}
