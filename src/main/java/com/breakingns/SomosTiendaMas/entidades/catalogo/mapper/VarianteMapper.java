package com.breakingns.SomosTiendaMas.entidades.catalogo.mapper;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.variante1.VarianteCrearDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.variante1.VarianteDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.variante1.VarianteListaDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Producto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Variante;

import java.util.ArrayList;
import java.util.List;

public final class VarianteMapper {

    private VarianteMapper() {}

    public static VarianteDTO toDto(Variante v) {
        if (v == null) return null;
        VarianteDTO dto = new VarianteDTO();
        dto.setId(v.getId());
        dto.setProductoId(v.getProducto() != null ? v.getProducto().getId() : null);
        dto.setSku(v.getSku());
        // sku resuelto: producto.slug + '-' + variante.sku when possible
        if (v.getProducto() != null && v.getProducto().getSlug() != null && v.getSku() != null) {
            dto.setSkuResuelto(v.getProducto().getSlug() + "-" + v.getSku());
        } else {
            dto.setSkuResuelto(v.getSku());
        }
        dto.setAttributesJson(v.getAttributesJson());
        dto.setAttributesHash(v.getAttributesHash());
        dto.setEsDefault(v.isEsDefault());
        dto.setActivo(v.isActivo());
        dto.setCreatedAt(v.getCreatedAt());
        dto.setUpdatedAt(v.getUpdatedAt());
        dto.setVersion(v.getVersion());
        // precio/stock resueltos se deben rellenar en servicio si se dispone de datos
        return dto;
    }

    public static VarianteListaDTO toListaDto(Variante v) {
        if (v == null) return null;
        VarianteListaDTO dto = new VarianteListaDTO();
        dto.setId(v.getId());
        dto.setProductoId(v.getProducto() != null ? v.getProducto().getId() : null);
        if (v.getProducto() != null && v.getProducto().getSlug() != null && v.getSku() != null) {
            dto.setSkuResuelto(v.getProducto().getSlug() + "-" + v.getSku());
        } else {
            dto.setSkuResuelto(v.getSku());
        }
        dto.setEsDefault(v.isEsDefault());
        dto.setActivo(v.isActivo());
        return dto;
    }

    public static Variante fromCrearDto(VarianteCrearDTO dto, Producto producto) {
        if (dto == null) return null;
        Variante v = new Variante();
        v.setSku(dto.getSku());
        v.setAttributesJson(dto.getAttributesJson());
        v.setAttributesHash(dto.getAttributesHash());
        v.setEsDefault(dto.getEsDefault() != null ? dto.getEsDefault() : false);
        v.setActivo(dto.getActivo() != null ? dto.getActivo() : true);
        if (producto != null) v.setProducto(producto);
        return v;
    }

    public static List<VarianteDTO> toDtoList(List<Variante> variantes) {
        if (variantes == null) return null;
        List<VarianteDTO> out = new ArrayList<>();
        for (Variante v : variantes) out.add(toDto(v));
        return out;
    }

}
