package com.breakingns.SomosTiendaMas.entidades.catalogo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "producto_valor", uniqueConstraints = {
        @UniqueConstraint(name = "ux_producto_valor_unico", columnNames = {"producto_id", "valor_id"})
}, indexes = {
        @Index(name = "ix_producto_valor_producto", columnList = "producto_id"),
        @Index(name = "ix_producto_valor_valor", columnList = "valor_id")
})
@Getter
@Setter
public class ProductoValor extends BaseEntidadAuditada {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "valor_id", nullable = false)
    private OpcionValor valor;
}