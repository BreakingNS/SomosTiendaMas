package com.breakingns.SomosTiendaMas.entidades.catalogo.mapper;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.vendedor.VendedorActualizarDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.vendedor.VendedorCrearDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.vendedor.VendedorResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.vendedor.VendedorResumenDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Vendedor;

import java.util.List;
import java.util.stream.Collectors;

public final class VendedorMapper {

    private VendedorMapper() {}

    public static VendedorResponseDTO toResponse(Vendedor v) {
        if (v == null) return null;
        return VendedorResponseDTO.builder()
                .id(v.getId())
                .userId(v.getUserId())
                .nombre(v.getNombre())
                .descripcion(v.getDescripcion())
                .rating(v.getRating())
                .createdAt(v.getCreatedAt())
                .updatedAt(v.getUpdatedAt())
                .deletedAt(v.getDeletedAt())
                .build();
    }

    public static VendedorResumenDTO toResumen(Vendedor v) {
        if (v == null) return null;
        VendedorResumenDTO r = new VendedorResumenDTO();
        r.setId(v.getId());
        r.setNombre(v.getNombre());
        r.setRating(v.getRating());
        return r;
    }

    public static List<VendedorResumenDTO> toResumenList(List<Vendedor> list) {
        if (list == null) return List.of();
        return list.stream().map(VendedorMapper::toResumen).collect(Collectors.toList());
    }

    public static Vendedor fromCrear(VendedorCrearDTO dto) {
        if (dto == null) return null;
        Vendedor v = new Vendedor();
        v.setUserId(dto.getUserId());
        v.setNombre(dto.getNombre());
        v.setDescripcion(dto.getDescripcion());
        v.setRating(dto.getRating());
        return v;
    }

    public static void applyActualizar(VendedorActualizarDTO dto, Vendedor entidad) {
        if (dto == null || entidad == null) return;
        if (dto.getNombre() != null) entidad.setNombre(dto.getNombre());
        if (dto.getDescripcion() != null) entidad.setDescripcion(dto.getDescripcion());
        if (dto.getRating() != null) entidad.setRating(dto.getRating());
    }
}