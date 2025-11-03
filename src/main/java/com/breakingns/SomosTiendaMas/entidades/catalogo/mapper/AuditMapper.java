package com.breakingns.SomosTiendaMas.entidades.catalogo.mapper;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.shared.AuditDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.BaseEntidadAuditada;

public final class AuditMapper {

    private AuditMapper() {}

    /**
     * Convierte la parte de auditoría de la entidad a AuditDTO.
     * Nota: AuditDTO actual en tu proyecto solo contiene createdAt, por eso solo se rellena ese campo.
     */
    public static AuditDTO fromEntidad(BaseEntidadAuditada entidad) {
        if (entidad == null) return null;
        AuditDTO dto = new AuditDTO();
        dto.setCreatedAt(entidad.getCreatedAt());
        return dto;
    }
}