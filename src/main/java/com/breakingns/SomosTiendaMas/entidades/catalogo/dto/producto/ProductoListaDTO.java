package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto;

import lombok.Data;

@Data
public class ProductoListaDTO {
    private Long id;
    private String nombre;
    private String slug;
    private Long marcaId;
    private Long categoriaId;
    private String sku;
    private Long precioCentavos; // opcional, si quieres mostrar precio en lista
    private long disponible;     // opcional, resumen de inventario
}