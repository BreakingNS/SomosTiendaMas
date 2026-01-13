package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.variante1.VarianteListaDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.enums.CondicionProducto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.enums.EstadoProducto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.enums.VisibilidadProducto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

import java.time.LocalDateTime;

@Data
@Builder
public class ProductoResponseDTO {
    private Long id;
    private String nombre;
    private String slug;
    private String descripcion;
    private Long marcaId;
    private String marcaNombre;
    private Long categoriaId;
    
    // nuevos campos para breadcrumb directo desde el producto
    private Long idCategoriaPadre;
    private Long idCategoriaHija;
    private String nombreCategoriaPadre;
    private String nombreCategoriaHija;

    private EstadoProducto estado;
    private VisibilidadProducto visibilidad;
    private String metadataJson;
    private String atributosJson;
    private String sku;

    // nuevo
    private CondicionProducto condicion;

    // auditoría / soft-delete
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    // optimistic locking
    private Long version;

    // nuevos campos para la ficha
    private String garantia;
    private String politicaDevoluciones;

    // campos resueltos durante migración
    private String skuResuelto;

    // variantes resumen
    private List<VarianteListaDTO> variantes;
}