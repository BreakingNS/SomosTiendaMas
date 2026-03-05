package com.breakingns.SomosTiendaMas.entidades.catalogo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "vendedor_auditoria", indexes = {
    @Index(name = "ix_vendedor_auditoria_vendedor", columnList = "vendedor_id"),
    @Index(name = "ix_vendedor_auditoria_created_at", columnList = "created_at")
})
@Getter
@Setter
public class VendedorAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vendedor_id", nullable = false)
    private Long vendedorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", length = 40, nullable = false)
    private TipoCambio tipoCambio;

    @Column(name = "field_name", length = 100)
    private String fieldName;

    @Column(name = "old_value", columnDefinition = "text")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "text")
    private String newValue;

    @Column(name = "reason", length = 1000)
    private String reason;

    @Column(name = "changed_by")
    private Long changedBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum TipoCambio {
        ESTADO,
        ADVERTENCIA_AGREGADA,
        ADVERTENCIA_REMOVIDA,
        ACTUALIZADO,
        NOTA
    }

}
