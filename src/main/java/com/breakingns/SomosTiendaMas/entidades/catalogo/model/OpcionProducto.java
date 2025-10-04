package com.breakingns.SomosTiendaMas.entidades.catalogo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "opcion_productO", indexes = {
        @Index(name = "ix_opcion_producto_producto", columnList = "producto_id")
})
@Getter
@Setter
public class OpcionProducto extends BaseEntidadAuditada {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(name = "orden", nullable = false)
    private Integer orden = 0;
}
