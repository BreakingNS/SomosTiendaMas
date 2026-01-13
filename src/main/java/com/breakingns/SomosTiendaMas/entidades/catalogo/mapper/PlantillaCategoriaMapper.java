package com.breakingns.SomosTiendaMas.entidades.catalogo.mapper;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.plantilla.*;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Mapper estático para PlantillaCategoria y objetos relacionados.
 * Diseñado para ser simple y defensivo (null-safe).
 */
public final class PlantillaCategoriaMapper {

    private PlantillaCategoriaMapper() {}

    public static PlantillaCategoriaDTO toDTO(PlantillaCategoria entidad) {
        if (entidad == null) return null;
        PlantillaCategoriaDTO dto = new PlantillaCategoriaDTO();
        dto.setId(entidad.getId());
        dto.setCategoriaId(entidad.getCategoria() != null ? entidad.getCategoria().getId() : null);
        dto.setCampos(mapCamposToDto(entidad.getCampos()));
        return dto;
    }

    public static PlantillaCategoriaResponseDTO toResponseDTO(PlantillaCategoria entidad) {
        if (entidad == null) return null;
        return PlantillaCategoriaResponseDTO.builder()
                .id(entidad.getId())
                .categoriaId(entidad.getCategoria() != null ? entidad.getCategoria().getId() : null)
                // entidad no define nombre/descripcion en el modelo actual -> quedan null
                .nombre(null)
                .descripcion(null)
                .opcionIds(new ArrayList<>())
                .createdAt(entidad.getCreatedAt())
                .updatedAt(entidad.getUpdatedAt())
                .deletedAt(entidad.getDeletedAt())
                .build();
    }

    public static PlantillaCategoriaResumenDTO toResumenDTO(PlantillaCategoria entidad) {
        if (entidad == null) return null;
        PlantillaCategoriaResumenDTO dto = new PlantillaCategoriaResumenDTO();
        dto.setId(entidad.getId());
        dto.setCategoriaId(entidad.getCategoria() != null ? entidad.getCategoria().getId() : null);
        dto.setNombre(null); // no hay campo nombre en la entidad actual
        return dto;
    }

    public static PlantillaCategoria fromCrearDTO(PlantillaCategoriaCrearDTO dto) {
        if (dto == null) return null;
        PlantillaCategoria entidad = new PlantillaCategoria();
        if (dto.getCategoriaId() != null) {
            Categoria c = new Categoria();
            c.setId(dto.getCategoriaId());
            entidad.setCategoria(c);
        }
        // Nombre/descripcion no existen en la entidad actual; si se agregan allí, asignar aquí.
        return entidad;
    }

    public static void updateFromActualizarDTO(PlantillaCategoria entidad, PlantillaCategoriaActualizarDTO dto) {
        if (entidad == null || dto == null) return;
        // La entidad actual no tiene nombre/descripcion/opcionIds; si se agregan, aquí aplicarlos condicionalmente.
    }

    // ---- PlantillaCampo mappers ----
    public static PlantillaCampoDTO mapCampoToDto(PlantillaCampo campo) {
        if (campo == null) return null;
        PlantillaCampoDTO dto = new PlantillaCampoDTO();
        dto.setId(campo.getId());
        dto.setNombre(campo.getNombre());
        dto.setSlug(campo.getSlug());
        dto.setTipo(campo.getTipo());
        dto.setOpcionesJson(campo.getOpcionesJson());
        dto.setOrden(campo.getOrden());
        dto.setRequerido(campo.isRequerido());
        return dto;
    }

    public static List<PlantillaCampoDTO> mapCamposToDto(List<PlantillaCampo> campos) {
        if (campos == null) return Collections.emptyList();
        return campos.stream()
                .filter(Objects::nonNull)
                .map(PlantillaCategoriaMapper::mapCampoToDto)
                .collect(Collectors.toList());
    }

    // Si en el futuro se necesita crear PlantillaCampo desde DTO, agregar aquí métodos inversos.
}