package com.breakingns.SomosTiendaMas.entidades.catalogo.mapper;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.opcion.OpcionResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.opcion.OpcionResumenDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.VarianteOpcion;

public final class VarianteOpcionMapper {

    private VarianteOpcionMapper() {}

    public static OpcionResponseDTO toResponseFromRelacion(VarianteOpcion po) {
        if (po == null) return null;
        var o = po.getOpcion();
        return OpcionResponseDTO.builder()
                .id(o.getId())
                .productoId(po.getVariante() != null && po.getVariante().getProducto() != null ? po.getVariante().getProducto().getId() : null)
                .nombre(o.getNombre())
                .orden(po.getOrden() != null ? po.getOrden() : o.getOrden())
                .tipo(o.getTipo())
                .createdAt(o.getCreatedAt())
                .updatedAt(o.getUpdatedAt())
                .deletedAt(o.getDeletedAt())
                .build();
    }

    public static OpcionResumenDTO toResumenFromRelacion(VarianteOpcion po) {
        if (po == null) return null;
        OpcionResumenDTO r = new OpcionResumenDTO();
        r.setId(po.getOpcion().getId());
        r.setNombre(po.getOpcion().getNombre());
        r.setOrden(po.getOrden() != null ? po.getOrden() : po.getOpcion().getOrden());
        r.setTipo(po.getOpcion().getTipo());
        return r;
    }
}