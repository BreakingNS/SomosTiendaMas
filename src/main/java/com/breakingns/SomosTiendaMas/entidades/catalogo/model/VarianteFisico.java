package com.breakingns.SomosTiendaMas.entidades.catalogo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "variante_fisico", indexes = {
        @Index(name = "ix_variante_fisico_variante", columnList = "variante_id")
})
@Getter
@Setter
public class VarianteFisico extends BaseEntidadAuditada {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variante_id", nullable = false, unique = true)
    private Variante variante;

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

    @PrePersist
    @PreUpdate
    private void validateDefaultVarianteFields() {
        if (this.variante != null && this.variante.isEsDefault()) {
            // Para la variante por defecto, requerimos que los campos físicos estén completos (no null)
            if (this.widthMm == null || this.heightMm == null || this.depthMm == null || this.weightGrams == null
                    || this.packageWidthMm == null || this.packageHeightMm == null || this.packageDepthMm == null || this.packageWeightGrams == null) {
                throw new IllegalStateException("Variante por defecto requiere todos los campos físicos no nulos");
            }
        }
    }
}
