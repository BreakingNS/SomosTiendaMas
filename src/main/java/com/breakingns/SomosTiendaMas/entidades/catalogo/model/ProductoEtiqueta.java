package com.breakingns.SomosTiendaMas.entidades.catalogo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "producto_etiqueta", uniqueConstraints = {
        @UniqueConstraint(name = "ux_producto_etiqueta", columnNames = {"producto_id", "etiqueta_id"})
}, indexes = {
        @Index(name = "ix_pe_producto", columnList = "producto_id"),
        @Index(name = "ix_pe_etiqueta", columnList = "etiqueta_id")
})
@Getter
@Setter
public class ProductoEtiqueta extends BaseEntidadAuditada {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "etiqueta_id", nullable = false)
    private Etiqueta etiqueta;
}
