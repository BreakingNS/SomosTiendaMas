package com.breakingns.SomosTiendaMas.entidades.catalogo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "variante_producto", indexes = {
        @Index(name = "ux_variante_sku", columnList = "sku", unique = true),
        @Index(name = "ix_variante_producto", columnList = "producto_id")
})
@Getter
@Setter
public class VarianteProducto extends BaseEntidadAuditada {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false, length = 80, unique = true)
    private String sku;

    @Column(name = "codigo_barras", length = 80)
    private String codigoBarras;

    // Dimensiones y peso (en unidades básicas)
    @Column(name = "peso_gramos")
    private Long pesoGramos;

    @Column(name = "alto_mm")
    private Integer altoMm;

    @Column(name = "ancho_mm")
    private Integer anchoMm;

    @Column(name = "largo_mm")
    private Integer largoMm;

    @Column(name = "metadata_json", columnDefinition = "TEXT")
    private String metadataJson;
}
