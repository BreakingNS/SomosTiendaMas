package com.breakingns.SomosTiendaMas.entidades.catalogo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "vendedor",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_vendedor_empresa", columnNames = {"empresa_id"}),
        @UniqueConstraint(name = "uk_vendedor_usuario", columnNames = {"usuario_id"}),
        @UniqueConstraint(name = "uk_vendedor_slug", columnNames = {"slug"})
    },
    indexes = {
        @Index(name = "ix_vendedor_empresa", columnList = "empresa_id"),
        @Index(name = "ix_vendedor_usuario", columnList = "usuario_id"),
        @Index(name = "ix_vendedor_status", columnList = "status")
    }
)
@Getter
@Setter
public class Vendedor extends BaseEntidadAuditada {

    // FK a empresa (si el vendedor representa a una empresa)
    @Column(name = "empresa_id")
    private Long empresaId;

    // FK a usuario (si el vendedor es un usuario individual)
    @Column(name = "usuario_id")
    private Long usuarioId;

    // Nombre legal y display
    @Column(name = "nombre_legal", nullable = false, length = 200)
    private String nombreLegal;

    @Column(name = "display_name", length = 200)
    private String displayName;

    @Column(name = "slug", nullable = false, length = 200)
    private String slug;

    // Estado del vendedor
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EstatusVendedor status = EstatusVendedor.PENDIENTE;

    // Strikes y baneo temporal
    @Column(name = "strikes_count", nullable = false)
    private Integer strikesCount = 0;

    @Column(name = "banned_until")
    private LocalDateTime bannedUntil;

    @Column(name = "verified", nullable = false)
    private Boolean verified = false;

    // Metadatos flexibles (usar TEXT para aceptar JSON en formato String)
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    // Convenience enum
    public enum EstatusVendedor {
        PENDIENTE,
        ACTIVO,
        PAUSADO,
        SUSPENDIDO,
        BLOQUEADO
    }

    // Validación: exactamente uno de empresaId o usuarioId debe estar presente
    @PrePersist
    @PreUpdate
    private void validateOwnerXor() {
        boolean hasEmpresa = this.empresaId != null;
        boolean hasUsuario = this.usuarioId != null;
        if (hasEmpresa == hasUsuario) { // both true or both false
            throw new IllegalStateException("Vendedor debe tener exactamente uno de empresaId o usuarioId (XOR)");
        }
        if (this.strikesCount == null) this.strikesCount = 0;
        if (this.verified == null) this.verified = false;
        if (this.status == null) this.status = EstatusVendedor.PENDIENTE;
    }

    public boolean isBanned() {
        return bannedUntil != null && bannedUntil.isAfter(LocalDateTime.now());
    }

}
