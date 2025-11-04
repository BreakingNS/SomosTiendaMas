package com.breakingns.SomosTiendaMas.entidades.catalogo.mapper;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.opcion.OpcionCrearDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.opcion.OpcionResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.opcion.OpcionResumenDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.opcion.OpcionActualizarDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Opcion;

public final class OpcionMapper {

    private OpcionMapper() {}

    public static OpcionResponseDTO toResponse(Opcion o) {
        if (o == null) return null;
        return OpcionResponseDTO.builder()
                .id(o.getId())
                .productoId(null)
                .nombre(o.getNombre())
                .orden(o.getOrden())
                .tipo(o.getTipo())
                .createdAt(o.getCreatedAt())
                .updatedAt(o.getUpdatedAt())
                .deletedAt(o.getDeletedAt())
                .build();
    }

    public static OpcionResumenDTO toResumen(Opcion o) {
        if (o == null) return null;
        OpcionResumenDTO r = new OpcionResumenDTO();
        r.setId(o.getId());
        r.setNombre(o.getNombre());
        r.setOrden(o.getOrden());
        r.setTipo(o.getTipo());
        return r;
    }

    public static Opcion fromCrear(OpcionCrearDTO dto) {
        if (dto == null) return null;
        Opcion e = new Opcion();
        e.setNombre(dto.getNombre());
        e.setOrden(dto.getOrden() != null ? dto.getOrden() : 0);
        // producto asignar en servicio si dto.productoId != null
        e.setTipo(null);
        return e;
    }

    public static void applyActualizar(OpcionActualizarDTO dto, Opcion entidad) {
        if (dto == null || entidad == null) return;
        if (dto.getNombre() != null) entidad.setNombre(dto.getNombre());
        if (dto.getOrden() != null) entidad.setOrden(dto.getOrden());
    }
}