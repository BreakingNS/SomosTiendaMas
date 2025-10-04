package com.breakingns.SomosTiendaMas.entidades.catalogo.model;

import com.breakingns.SomosTiendaMas.entidades.catalogo.enums.EstadoProducto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.enums.VisibilidadProducto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "producto", indexes = {
        @Index(name = "ux_producto_slug", columnList = "slug", unique = true)
})
@Getter
@Setter
public class Producto extends BaseEntidadAuditada {

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(nullable = false, length = 220, unique = true)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "marca_id")
    private Marca marca;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private EstadoProducto estado = EstadoProducto.BORRADOR;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private VisibilidadProducto visibilidad = VisibilidadProducto.PUBLICO;

    // Atributos libres (JSON como TEXT por ahora)
    @Column(name = "metadata_json", columnDefinition = "TEXT")
    private String metadataJson;
}
