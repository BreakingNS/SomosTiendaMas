package com.breakingns.SomosTiendaMas.entidades.catalogo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "imagen_producto", indexes = {
        @Index(name = "ix_img_variante", columnList = "variante_id")
})
@Getter
@Setter
public class ImagenVariante extends BaseEntidadAuditada {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variante_id")
    private Variante variante;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(length = 250)
    private String alt;

    @Column(nullable = false)
    private Integer orden = 0;

    // conveniencia: exponer producto por compatibilidad con servicios/mappers
    public Producto getProducto() {
        return this.variante != null ? this.variante.getProducto() : null;
    }

    public void setProducto(Producto producto) {
        if (this.variante == null) this.variante = new Variante();
        this.variante.setProducto(producto);
    }
}