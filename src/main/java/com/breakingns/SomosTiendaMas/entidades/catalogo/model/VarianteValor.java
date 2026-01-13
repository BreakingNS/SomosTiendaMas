package com.breakingns.SomosTiendaMas.entidades.catalogo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "variante_valor", uniqueConstraints = {
        @UniqueConstraint(name = "ux_variante_valor_unico", columnNames = {"variante_id", "valor_id"})
}, indexes = {
        @Index(name = "ix_variante_valor_variante", columnList = "variante_id"),
        @Index(name = "ix_variante_valor_valor", columnList = "valor_id")
})
@Getter
@Setter
public class VarianteValor extends BaseEntidadAuditada {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "variante_id", nullable = false)
    private Variante variante;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "valor_id", nullable = false)
    private OpcionValor valor;
}