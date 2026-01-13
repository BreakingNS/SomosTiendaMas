package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto;

import com.breakingns.SomosTiendaMas.entidades.catalogo.enums.CondicionProducto;

import lombok.Data;

@Data
public class ProductoListaDTO {
    private Long id;
    private String nombre;
    private String slug;
    private Long marcaId;
    private Long categoriaId;
    private String sku;
    // disponible movido a variantes (no pertenece al DTO de producto)

    // nuevo
    private CondicionProducto condicion;
    // campos resueltos para migración
    private String skuResuelto;
    
}