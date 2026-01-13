package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_valor;

import java.util.List;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.opcion.OpcionValorResponseDTO;

public class ProductoValoresPorProductoDTO {
    private Long productoId;
    private List<OpcionValorResponseDTO> valores;

    public ProductoValoresPorProductoDTO() {}
    public ProductoValoresPorProductoDTO(Long productoId, List<OpcionValorResponseDTO> valores) {
        this.productoId = productoId;
        this.valores = valores;
    }

    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }

    public List<OpcionValorResponseDTO> getValores() { return valores; }
    public void setValores(List<OpcionValorResponseDTO> valores) { this.valores = valores; }
}