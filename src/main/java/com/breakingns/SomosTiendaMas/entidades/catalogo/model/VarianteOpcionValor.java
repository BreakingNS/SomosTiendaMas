package com.breakingns.SomosTiendaMas.entidades.catalogo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "variante_opcion_valor", uniqueConstraints = {
        @UniqueConstraint(name = "ux_variante_valor_unico", columnNames = {"variante_id", "valor_id"})
}, indexes = {
        @Index(name = "ix_vov_variante", columnList = "variante_id"),
        @Index(name = "ix_vov_valor", columnList = "valor_id")
})
@Getter
@Setter
public class VarianteOpcionValor extends BaseEntidadAuditada {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "variante_id", nullable = false)
    private VarianteProducto variante;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "valor_id", nullable = false)
    private ValorOpcionProducto valor;
}
