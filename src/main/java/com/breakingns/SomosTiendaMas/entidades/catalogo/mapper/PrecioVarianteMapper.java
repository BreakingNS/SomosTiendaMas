package com.breakingns.SomosTiendaMas.entidades.catalogo.mapper;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.precio.PrecioVarianteResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.PrecioVariante;

public class PrecioVarianteMapper {
    public static PrecioVarianteResponseDTO toResponse(PrecioVariante p) {
        return PrecioVarianteResponseDTO.builder()
                .id(p.getId())
                .varianteId(p.getVariante() != null ? p.getVariante().getId() : null)
                .montoCentavos(p.getMontoCentavos())
                .moneda(p.getMoneda())
                .tipo(p.getTipo())
                .activo(Boolean.TRUE.equals(p.getActivo()))
                .build();
    }
}
