package com.breakingns.SomosTiendaMas.entidades.catalogo.mapper;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.opcion.ValorOpcionProductoResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.ValorOpcionProducto;

public class ValorOpcionProductoMapper {
    public static ValorOpcionProductoResponseDTO toResponse(ValorOpcionProducto v) {
        return ValorOpcionProductoResponseDTO.builder()
                .id(v.getId())
                .opcionId(v.getOpcion() != null ? v.getOpcion().getId() : null)
                .valor(v.getValor())
                .slug(v.getSlug())
                .orden(v.getOrden())
                .build();
    }
}
