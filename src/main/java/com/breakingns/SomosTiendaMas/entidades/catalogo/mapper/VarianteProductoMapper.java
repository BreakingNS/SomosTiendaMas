package com.breakingns.SomosTiendaMas.entidades.catalogo.mapper;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.variante.VarianteCrearDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.variante.VarianteResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Producto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.VarianteProducto;

public class VarianteProductoMapper {

    public static VarianteProducto toEntity(VarianteCrearDTO dto, Producto producto) {
        VarianteProducto v = new VarianteProducto();
        v.setProducto(producto);
        v.setSku(dto.getSku());
        v.setCodigoBarras(dto.getCodigoBarras());
        v.setPesoGramos(dto.getPesoGramos());
        v.setAltoMm(dto.getAltoMm());
        v.setAnchoMm(dto.getAnchoMm());
        v.setLargoMm(dto.getLargoMm());
        v.setMetadataJson(dto.getMetadataJson());
        return v;
    }

    public static VarianteResponseDTO toResponse(VarianteProducto v) {
        return VarianteResponseDTO.builder()
                .id(v.getId())
                .productoId(v.getProducto() != null ? v.getProducto().getId() : null)
                .sku(v.getSku())
                .codigoBarras(v.getCodigoBarras())
                .pesoGramos(v.getPesoGramos())
                .altoMm(v.getAltoMm())
                .anchoMm(v.getAnchoMm())
                .largoMm(v.getLargoMm())
                .metadataJson(v.getMetadataJson())
                .build();
    }
}
