package com.breakingns.SomosTiendaMas.entidades.catalogo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "opcion_valor", uniqueConstraints = {
        @UniqueConstraint(name = "ux_valor_por_opcion", columnNames = {"opcion_id", "valor"})
}, indexes = {
        @Index(name = "ix_opcion_valor_opcion", columnList = "opcion_id")
})
@Getter
@Setter
public class OpcionValor extends BaseEntidadAuditada {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "opcion_id", nullable = false)
    private Opcion opcion;

    @Column(nullable = false, length = 120)
    private String valor;

    @Column(length = 160)
    private String slug;

    @Column(name = "orden", nullable = false)
    private Integer orden = 0;
}
