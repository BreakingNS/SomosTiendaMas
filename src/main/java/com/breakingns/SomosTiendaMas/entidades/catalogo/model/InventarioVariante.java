package com.breakingns.SomosTiendaMas.entidades.catalogo.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventario_producto", indexes = {
    @Index(name = "idx_inventario_variante_variante_id", columnList = "variante_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventarioVariante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // vínculo a la variante — 1 inventario por variante
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variante_id", nullable = false, unique = true)
    private Variante variante;
    /* 
    // referencia directa al producto (comodín para consultas y compatibilidad con mappers/servicios)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id")
    private Producto producto;
    */
    // stock físico esperado (on-hand)
    @Column(name = "on_hand", nullable = false)
    private Integer onHand = 0;

    // cantidad reservada (por carritos/pedidos no confirmados)
    @Column(name = "reserved", nullable = false)
    private Integer reserved = 0;

    // opcional: ubicación libre del vendedor (ej: "estanteria A3", "deposito1:seccion2")
    @Column(name = "ubicacion", length = 255)
    private String ubicacion;

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
