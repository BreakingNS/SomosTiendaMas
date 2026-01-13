package com.breakingns.SomosTiendaMas.entidades.catalogo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "producto_opcion", uniqueConstraints = {
        @UniqueConstraint(name = "ux_producto_opcion", columnNames = {"producto_id", "opcion_id"})
}, indexes = {
        @Index(name = "ix_producto_opcion_producto", columnList = "producto_id"),
        @Index(name = "ix_producto_opcion_opcion", columnList = "opcion_id")
})
@Getter
@Setter
public class ProductoOpcion extends BaseEntidadAuditada {
        // -----------------------------
        // Relaciones
        // -----------------------------
        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "producto_id", nullable = false)
        private Producto producto;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "opcion_id", nullable = false)
        private Opcion opcion;

        // -----------------------------
        // Configuración de la opción en el producto
        // -----------------------------
        @Column(name = "orden", nullable = false)
        private Integer orden = 0;

        @Column(name = "requerido", nullable = false)
        private boolean requerido = false;

        @Column(name = "activo", nullable = false)
        private boolean activo = true;

        // -----------------------------
        // Campos libres / metadata
        // -----------------------------
        @Column(name = "metadata_json", columnDefinition = "TEXT")
        private String metadataJson;
}