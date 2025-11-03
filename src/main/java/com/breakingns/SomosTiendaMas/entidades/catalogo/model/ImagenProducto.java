// VISTO BUENO
package com.breakingns.SomosTiendaMas.entidades.catalogo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "imagen_producto", indexes = {
        @Index(name = "ix_img_producto", columnList = "producto_id")
})
@Getter
@Setter
public class ImagenProducto extends BaseEntidadAuditada {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(length = 250)
    private String alt;

    @Column(nullable = false)
    private Integer orden = 0;
}
