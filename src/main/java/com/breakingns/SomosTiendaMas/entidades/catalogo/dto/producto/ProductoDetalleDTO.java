package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

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
    private List<Long> imagenIds;
    private List<String> imagenUrls; // si tu mapper provee urls
    private Long precioActualCentavos;
    private Boolean precioActivo;
    private long disponible; // desde InventarioProducto

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