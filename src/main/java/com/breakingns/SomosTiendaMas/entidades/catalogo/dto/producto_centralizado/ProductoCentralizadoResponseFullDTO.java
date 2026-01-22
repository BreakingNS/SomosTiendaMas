package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_centralizado;

import java.util.List;

public class ProductoCentralizadoResponseFullDTO {
    private ProductoCentralizadoResponseDTO producto;
    private List<VarianteCentralizadaResponseDTO> variantes;

    public ProductoCentralizadoResponseDTO getProducto() { return producto; }
    public void setProducto(ProductoCentralizadoResponseDTO producto) { this.producto = producto; }
    public List<VarianteCentralizadaResponseDTO> getVariantes() { return variantes; }
    public void setVariantes(List<VarianteCentralizadaResponseDTO> variantes) { this.variantes = variantes; }
}
