package com.breakingns.SomosTiendaMas.entidades.catalogo.mapper;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.opcion.OpcionValorResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_valor.ProductoValoresPorProductoDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.ProductoValor;

import java.util.List;
import java.util.stream.Collectors;

public class ProductoValorMapper {
    public static ProductoValoresPorProductoDTO toProductoValoresPorProductoDTO(Long productoId, List<ProductoValor> relaciones) {
        List<OpcionValorResponseDTO> valores = relaciones.stream()
                .filter(r -> r.getDeletedAt() == null && r.getValor() != null && r.getValor().getDeletedAt() == null)
                .map(r -> OpcionValorMapper.toResponse(r.getValor()))
                .collect(Collectors.toList());
        return new ProductoValoresPorProductoDTO(productoId, valores);
    }
}