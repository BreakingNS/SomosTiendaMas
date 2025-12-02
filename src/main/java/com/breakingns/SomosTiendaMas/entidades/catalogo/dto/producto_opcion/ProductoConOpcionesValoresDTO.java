package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_opcion;

import java.util.List;

public class ProductoConOpcionesValoresDTO {
    private Long productoId;
    private List<OpcionConValoresDTO> opciones;

    public ProductoConOpcionesValoresDTO() {}

    public ProductoConOpcionesValoresDTO(Long productoId, List<OpcionConValoresDTO> opciones) {
        this.productoId = productoId;
        this.opciones = opciones;
    }

    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }
    public List<OpcionConValoresDTO> getOpciones() { return opciones; }
    public void setOpciones(List<OpcionConValoresDTO> opciones) { this.opciones = opciones; }
}
