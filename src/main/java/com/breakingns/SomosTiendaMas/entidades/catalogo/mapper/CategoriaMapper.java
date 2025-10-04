package com.breakingns.SomosTiendaMas.entidades.catalogo.mapper;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.categoria.CategoriaResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Categoria;

public class CategoriaMapper {
    public static CategoriaResponseDTO toResponse(Categoria c) {
        return CategoriaResponseDTO.builder()
                .id(c.getId())
                .nombre(c.getNombre())
                .slug(c.getSlug())
                .descripcion(c.getDescripcion())
                .categoriaPadreId(c.getCategoriaPadre() != null ? c.getCategoriaPadre().getId() : null)
                .build();
    }
}
