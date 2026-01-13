package com.breakingns.SomosTiendaMas.entidades.catalogo.model;

import com.breakingns.SomosTiendaMas.entidades.catalogo.enums.TipoMovimientoInventario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "movimiento_inventario", indexes = {
        @Index(name = "ix_mov_variante", columnList = "variante_id"),
        @Index(name = "ix_mov_order_ref", columnList = "order_ref")
})
@Getter
@Setter
public class MovimientoInventario extends BaseEntidadAuditada {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "variante_id", nullable = false)
    private Variante variante;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private TipoMovimientoInventario tipo;

    @Column(nullable = false)
    private Long cantidad;

    // Idempotencia/correlación con pedidos/checkout
    @Column(name = "order_ref", length = 120)
    private String orderRef;

    // Referencia externa opcional (UUID, id de pedido, etc.)
    @Column(name = "referencia_id", length = 120)
    private String referenciaId;

    @Column(name = "metadata_json", columnDefinition = "TEXT")
    private String metadataJson;

    // conveniencia: exponer producto por compatibilidad con servicios/mappers
    public Producto getProducto() {
        return this.variante != null ? this.variante.getProducto() : null;
    }

    public void setProducto(Producto producto) {
        if (this.variante == null) this.variante = new Variante();
        this.variante.setProducto(producto);
    }
}
