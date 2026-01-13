package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_opcion;

import java.util.List;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.opcion.OpcionResumenDTO;

public class ProductoConOpcionesDTO {
    private Long productoId;
    private List<OpcionResumenDTO> opciones;

    public ProductoConOpcionesDTO() {}

    public ProductoConOpcionesDTO(Long productoId, List<OpcionResumenDTO> opciones) {
        this.productoId = productoId;
        this.opciones = opciones;
    }

    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }

    public List<OpcionResumenDTO> getOpciones() { return opciones; }
    public void setOpciones(List<OpcionResumenDTO> opciones) { this.opciones = opciones; }
}