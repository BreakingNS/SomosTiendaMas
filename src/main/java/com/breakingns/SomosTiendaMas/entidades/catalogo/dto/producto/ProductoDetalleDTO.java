package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto;

import lombok.Data;

import java.time.LocalDateTime;

import com.breakingns.SomosTiendaMas.entidades.catalogo.enums.CondicionProducto;

@Data
public class ProductoDetalleDTO {
    private Long id;
    private String nombre;
    private String slug;
    private String descripcion;
    private Long marcaId;
    private Long categoriaId;
    private String sku;
    private String atributosJson;
    private String metadataJson;

    // relaciones/embebidos
    // imágenes, precio y stock fueron movidos a `Variante`.
    // Se eliminan campos de imagenes/stock a favor de variantes.
    // campos "resueltos" durante la migración
    private String skuResuelto;
    

    // texto mostrable en la ficha
    private String garantia;
    private String politicaDevoluciones;

    // nuevo
    private CondicionProducto condicion;

    // auditoría
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}