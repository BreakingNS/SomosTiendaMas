package com.breakingns.SomosTiendaMas.entidades.catalogo.mapper;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.opcion.OpcionProductoResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.OpcionProducto;

public class OpcionProductoMapper {
    public static OpcionProductoResponseDTO toResponse(OpcionProducto o) {
        return OpcionProductoResponseDTO.builder()
                .id(o.getId())
                .productoId(o.getProducto() != null ? o.getProducto().getId() : null)
                .nombre(o.getNombre())
                .orden(o.getOrden())
                .build();
    }
}
