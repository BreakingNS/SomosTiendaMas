package com.breakingns.SomosTiendaMas.entidades.catalogo.mapper;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.marca.MarcaResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Marca;

public class MarcaMapper {
    public static MarcaResponseDTO toResponse(Marca m) {
        return MarcaResponseDTO.builder()
                .id(m.getId())
                .nombre(m.getNombre())
                .slug(m.getSlug())
                .descripcion(m.getDescripcion())
                .build();
    }
}
