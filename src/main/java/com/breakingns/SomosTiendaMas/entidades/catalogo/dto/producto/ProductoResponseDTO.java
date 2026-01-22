package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.opcion.OpcionResumenDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_centralizado.ProductoCentralizadoResponseDTO;

import java.util.List;

public class ProductoResponseDTO {

    private ProductoCentralizadoResponseDTO producto;
    private List<OpcionResumenDTO> opciones;

    public ProductoCentralizadoResponseDTO getProducto() {
        return producto;
    }

    public void setProducto(ProductoCentralizadoResponseDTO producto) {
        this.producto = producto;
    }

    public List<OpcionResumenDTO> getOpciones() {
        return opciones;
    }

    public void setOpciones(List<OpcionResumenDTO> opciones) {
        this.opciones = opciones;
    }

}