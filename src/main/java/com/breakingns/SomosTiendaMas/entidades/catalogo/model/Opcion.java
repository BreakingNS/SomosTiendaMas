package com.breakingns.SomosTiendaMas.entidades.catalogo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "opcion_producto"/* , indexes = {
        @Index(name = "ix_opcion_producto_producto", columnList = "producto_id")
}*/)
@Getter @Setter
public class Opcion extends BaseEntidadAuditada {

    // producto ahora opcional: si es plantilla, producto será null
    /*
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "producto_id", nullable = true)
    private Producto producto;
    */
    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(name = "orden", nullable = false)
    private Integer orden = 0;

    @Column(name = "tipo", length = 60)
    private String tipo;
}