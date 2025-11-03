package com.breakingns.SomosTiendaMas.entidades.catalogo.mapper;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.marca.MarcaResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Marca;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Categoria;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class MarcaMapper {

    private MarcaMapper() {}

    public static MarcaResponseDTO toResponse(Marca m) {
        if (m == null) return null;

        List<Long> categoriaIds = new ArrayList<>();
        if (m.getCategorias() != null && !m.getCategorias().isEmpty()) {
            categoriaIds = m.getCategorias().stream()
                    .filter(c -> c != null)
                    .map(Categoria::getId)
                    .collect(Collectors.toList());
        }

        Integer productosCount = m.getProductos() != null ? m.getProductos().size() : 0;

        Long creadaPorVendedorId = null;
        if (m.getCreadaPor() != null) {
            creadaPorVendedorId = m.getCreadaPor().getId();
        }

        return MarcaResponseDTO.builder()
                .id(m.getId())
                .nombre(m.getNombre())
                .slug(m.getSlug())
                .descripcion(m.getDescripcion())
                .createdAt(m.getCreatedAt())
                .updatedAt(m.getUpdatedAt())
                .deletedAt(m.getDeletedAt())
                .creadaPorUsuario(m.isCreadaPorUsuario())
                .creadaPorVendedorId(creadaPorVendedorId)
                .estadoModeracion(m.getEstadoModeracion() != null ? m.getEstadoModeracion().name() : null)
                .moderacionNotas(m.getModeracionNotas())
                .moderadoPor(m.getModeradoPor())
                .moderadoEn(m.getModeradoEn())
                .categoriaIds(categoriaIds)
                .productosCount(productosCount)
                .build();
    }
}