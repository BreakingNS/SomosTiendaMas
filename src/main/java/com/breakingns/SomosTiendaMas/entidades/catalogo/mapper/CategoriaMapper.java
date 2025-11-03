package com.breakingns.SomosTiendaMas.entidades.catalogo.mapper;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.categoria.CategoriaArbolDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.categoria.CategoriaResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.categoria.CategoriaResumenDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Categoria;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CategoriaMapper {

    public static CategoriaResponseDTO toResponse(Categoria c) {
        if (c == null) return null;
        return CategoriaResponseDTO.builder()
                .id(c.getId())
                .nombre(c.getNombre())
                .slug(c.getSlug())
                .descripcion(c.getDescripcion())
                .categoriaPadreId(c.getCategoriaPadre() != null ? c.getCategoriaPadre().getId() : null)
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .hijosCount(c.getHijos() != null ? c.getHijos().size() : 0)
                .build();
    }

    public static CategoriaResumenDTO toResumen(Categoria c) {
        if (c == null) return null;
        CategoriaResumenDTO r = new CategoriaResumenDTO();
        r.setId(c.getId());
        r.setNombre(c.getNombre());
        r.setSlug(c.getSlug());
        return r;
    }

    public static CategoriaArbolDTO toArbol(Categoria c) {
        if (c == null) return null;
        CategoriaArbolDTO dto = new CategoriaArbolDTO();
        dto.setId(c.getId());
        dto.setNombre(c.getNombre());
        dto.setSlug(c.getSlug());
        dto.setCategoriaPadreId(c.getCategoriaPadre() != null ? c.getCategoriaPadre().getId() : null);

        if (c.getHijos() != null && !c.getHijos().isEmpty()) {
            List<CategoriaArbolDTO> hijos = c.getHijos().stream()
                    .map(CategoriaMapper::toArbol)
                    .collect(Collectors.toList());
            dto.setHijos(hijos);
        } else {
            dto.setHijos(new ArrayList<>());
        }
        return dto;
    }

    public static List<CategoriaResumenDTO> toResumenList(List<Categoria> categorias) {
        if (categorias == null) return List.of();
        return categorias.stream().map(CategoriaMapper::toResumen).collect(Collectors.toList());
    }

    public static List<CategoriaArbolDTO> toArbolList(List<Categoria> categorias) {
        if (categorias == null) return List.of();
        return categorias.stream().map(CategoriaMapper::toArbol).collect(Collectors.toList());
    }
}