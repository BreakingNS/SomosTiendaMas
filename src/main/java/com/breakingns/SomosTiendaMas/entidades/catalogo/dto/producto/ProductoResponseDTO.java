package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto;

import com.breakingns.SomosTiendaMas.entidades.catalogo.enums.EstadoProducto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.enums.VisibilidadProducto;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ProductoResponseDTO {
    private Long id;
    private String nombre;
    private String slug;
    private String descripcion;
    private Long marcaId;
    private Long categoriaId;
    private EstadoProducto estado;
    private VisibilidadProducto visibilidad;
    private String metadataJson;
    private String atributosJson;
    private String sku;

    // auditoría / soft-delete
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    // optimistic locking
    private Long version;
}