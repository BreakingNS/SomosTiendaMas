// VISTO BUENO
package com.breakingns.SomosTiendaMas.entidades.catalogo.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventario_producto", indexes = {
        @Index(name = "idx_inventario_producto_producto_id", columnList = "producto_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventarioProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // vínculo al producto — 1 inventario por producto (ajustá según tu relación actual)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false, unique = true)
    private Producto producto;

    // stock físico esperado (on-hand)
    @Column(name = "on_hand", nullable = false)
    private Integer onHand = 0;

    // cantidad reservada (por carritos/pedidos no confirmados)
    @Column(name = "reserved", nullable = false)
    private Integer reserved = 0;

    // opcional: almacén/ubicación
    @Column(name = "almacen_id")
    private Long almacenId;

    // optimistic locking para evitar condiciones de carrera
    @Version
    private Long version;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // campo para soft-delete / trazabilidad
    @Column(name = "deleted_at")
    private java.time.LocalDateTime deletedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        if (this.onHand == null) this.onHand = 0;
        if (this.reserved == null) this.reserved = 0;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // helper
    @Transient
    public int getAvailable() {
        return Math.max(0, (onHand == null ? 0 : onHand) - (reserved == null ? 0 : reserved));
    }
}