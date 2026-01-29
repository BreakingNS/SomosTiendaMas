package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_centralizado;

import java.util.List;

public class ProductoCentralizadoResponseFullSimpleDTO {
    private ProductoCentralizadoResponseDTO producto;
    private List<VarianteCentralizadaResponseSimpleDTO> variantes;

    public ProductoCentralizadoResponseDTO getProducto() { return producto; }
    public void setProducto(ProductoCentralizadoResponseDTO producto) { this.producto = producto; }
    public List<VarianteCentralizadaResponseSimpleDTO> getVariantes() { return variantes; }
    public void setVariantes(List<VarianteCentralizadaResponseSimpleDTO> variantes) { this.variantes = variantes; }
}
