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
                .userId(v.getUsuarioId() != null ? v.getUsuarioId() : v.getEmpresaId())
                .nombre(v.getDisplayName() != null ? v.getDisplayName() : v.getNombreLegal())
                .descripcion(extractDescripcionFromMetadata(v.getMetadata()))
                .rating(null)
                .createdAt(v.getCreatedAt())
                .updatedAt(v.getUpdatedAt())
                .deletedAt(v.getDeletedAt())
                .build();
    }

    public static VendedorResumenDTO toResumen(Vendedor v) {
        if (v == null) return null;
        VendedorResumenDTO r = new VendedorResumenDTO();
        r.setId(v.getId());
        r.setNombre(v.getDisplayName() != null ? v.getDisplayName() : v.getNombreLegal());
        r.setRating(null);
        return r;
    }

    public static List<VendedorResumenDTO> toResumenList(List<Vendedor> list) {
        if (list == null) return List.of();
        return list.stream().map(VendedorMapper::toResumen).collect(Collectors.toList());
    }

    public static Vendedor fromCrear(VendedorCrearDTO dto) {
        if (dto == null) return null;
        Vendedor v = new Vendedor();
        // map DTO userId -> usuarioId
        v.setUsuarioId(dto.getUserId());
        // set both legal/display names from provided nombre
        v.setNombreLegal(dto.getNombre());
        v.setDisplayName(dto.getNombre());
        // store descripcion inside metadata as simple JSON {"descripcion":"..."}
        if (dto.getDescripcion() != null) {
            v.setMetadata("{\"descripcion\":\"" + escapeJson(dto.getDescripcion()) + "\"}");
        }
        return v;
    }

    public static void applyActualizar(VendedorActualizarDTO dto, Vendedor entidad) {
        if (dto == null || entidad == null) return;
        if (dto.getNombre() != null) {
            entidad.setDisplayName(dto.getNombre());
            entidad.setNombreLegal(dto.getNombre());
        }
        if (dto.getDescripcion() != null) {
            entidad.setMetadata("{\"descripcion\":\"" + escapeJson(dto.getDescripcion()) + "\"}");
        }
    }

    private static String extractDescripcionFromMetadata(String metadata) {
        if (metadata == null) return null;
        // very small heuristic: look for a simple {"descripcion":"..."} pattern
        try {
            int idx = metadata.indexOf("\"descripcion\":");
            if (idx >= 0) {
                int patternLen = "\"descripcion\":".length();
                int start = metadata.indexOf('"', idx + patternLen) + 1;
                int end = metadata.indexOf('"', start);
                if (start > 0 && end > start) return metadata.substring(start, end);
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static String escapeJson(String s) {
        if (s == null) return null;
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}