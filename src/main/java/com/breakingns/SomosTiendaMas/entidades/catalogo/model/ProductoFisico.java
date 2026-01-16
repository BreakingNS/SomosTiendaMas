package com.breakingns.SomosTiendaMas.entidades.catalogo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

// REVIEW: se va a utilizar solo VarianteFisico y no ProductoFisico, por lo que este controlador queda obsoleto
@Deprecated(since="2026-01-15", forRemoval=true)

@Entity
@Table(name = "producto_fisico", indexes = {
        @Index(name = "ix_producto_fisico_producto", columnList = "producto_id")
})
@Getter
@Setter
public class ProductoFisico extends BaseEntidadAuditada {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false, unique = true)
    private Producto producto;

    // Dimensiones en milímetros
    @Column(name = "width_mm")
    private Integer widthMm;

    @Column(name = "height_mm")
    private Integer heightMm;

    @Column(name = "depth_mm")
    private Integer depthMm;

    // Peso neto en gramos
    @Column(name = "weight_grams")
    private Integer weightGrams;

    // Dimensiones y peso del paquete (para envíos) en mm / gramos
    @Column(name = "package_width_mm")
    private Integer packageWidthMm;

    @Column(name = "package_height_mm")
    private Integer packageHeightMm;

    @Column(name = "package_depth_mm")
    private Integer packageDepthMm;

    @Column(name = "package_weight_grams")
    private Integer packageWeightGrams;
}