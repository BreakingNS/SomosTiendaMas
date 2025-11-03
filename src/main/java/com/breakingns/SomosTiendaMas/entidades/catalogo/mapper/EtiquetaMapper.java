package com.breakingns.SomosTiendaMas.entidades.catalogo.mapper;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.etiqueta.EtiquetaActualizarDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.etiqueta.EtiquetaCrearDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.etiqueta.EtiquetaResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.etiqueta.EtiquetaResumenDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Etiqueta;

public final class EtiquetaMapper {

    private EtiquetaMapper() {}

    public static EtiquetaResponseDTO toResponse(Etiqueta e) {
        if (e == null) return null;
        return EtiquetaResponseDTO.builder()
                .id(e.getId())
                .nombre(e.getNombre())
                .slug(e.getSlug())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .deletedAt(e.getDeletedAt())
                .build();
    }

    public static EtiquetaResumenDTO toResumen(Etiqueta e) {
        if (e == null) return null;
        EtiquetaResumenDTO r = new EtiquetaResumenDTO();
        r.setId(e.getId());
        r.setNombre(e.getNombre());
        r.setSlug(e.getSlug());
        return r;
    }

    public static Etiqueta fromCrear(EtiquetaCrearDTO dto) {
        if (dto == null) return null;
        Etiqueta e = new Etiqueta();
        e.setNombre(dto.getNombre());
        e.setSlug(dto.getSlug());
        return e;
    }

    public static void applyActualizar(EtiquetaActualizarDTO dto, Etiqueta entidad) {
        if (dto == null || entidad == null) return;
        if (dto.getNombre() != null) entidad.setNombre(dto.getNombre());
        if (dto.getSlug() != null) entidad.setSlug(dto.getSlug());
    }
}