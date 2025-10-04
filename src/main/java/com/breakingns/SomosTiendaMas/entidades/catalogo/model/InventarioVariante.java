package com.breakingns.SomosTiendaMas.entidades.catalogo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "inventario_variante", indexes = {
        @Index(name = "ux_inventario_variante", columnList = "variante_id", unique = true)
})
@Getter
@Setter
public class InventarioVariante extends BaseEntidadAuditada {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "variante_id", nullable = false, unique = true)
    private VarianteProducto variante;

    @Column(name = "on_hand", nullable = false)
    private Long onHand = 0L;

    @Column(name = "reserved", nullable = false)
    private Long reserved = 0L;

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    @Transient
    public long getAvailable() {
        return (onHand != null ? onHand : 0L) - (reserved != null ? reserved : 0L);
    }
}
