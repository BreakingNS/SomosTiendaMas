// VISTO BUENO
package com.breakingns.SomosTiendaMas.entidades.catalogo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "variante_opcion_valor", uniqueConstraints = {
        @UniqueConstraint(name = "ux_variante_valor_unico", columnNames = {"producto_id", "valor_id"})
}, indexes = {
        @Index(name = "ix_vov_producto", columnList = "producto_id"),
        @Index(name = "ix_vov_valor", columnList = "valor_id")
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
