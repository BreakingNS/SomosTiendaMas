package com.breakingns.SomosTiendaMas.entidades.catalogo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "valor_opcion_producto", uniqueConstraints = {
        @UniqueConstraint(name = "ux_valor_por_opcion", columnNames = {"opcion_id", "valor"})
}, indexes = {
        @Index(name = "ix_valor_opcion_opcion", columnList = "opcion_id")
})
@Getter
@Setter
public class ValorOpcionProducto extends BaseEntidadAuditada {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "opcion_id", nullable = false)
    private OpcionProducto opcion;

    @Column(nullable = false, length = 120)
    private String valor;

    @Column(length = 160)
    private String slug;

    @Column(name = "orden", nullable = false)
    private Integer orden = 0;
}
