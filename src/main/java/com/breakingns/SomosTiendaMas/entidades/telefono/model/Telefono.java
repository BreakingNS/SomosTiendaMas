package com.breakingns.SomosTiendaMas.entidades.telefono.model;

import com.breakingns.SomosTiendaMas.entidades.perfil.model.Perfil;
import com.breakingns.SomosTiendaMas.entidades.perfil_empresa.model.PerfilEmpresa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
@NoArgsConstructor
@Table(name = "telefono",
       indexes = {
           @Index(name = "ix_telefono_perfil_usuario", columnList = "perfil_usuario_id"),
           @Index(name = "ix_telefono_perfil_empresa", columnList = "perfil_empresa_id")
       }
)
public class Telefono {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "telefono_seq")
    @SequenceGenerator(name = "telefono_seq", sequenceName = "telefono_id_seq", allocationSize = 1)
    private Long id;

    // ---------------------------
    // IDENTIDAD / PROPIETARIO
    // ---------------------------
    // Exactly one of perfilUsuario or perfilEmpresa must be set (XOR). Validar en capa servicio o CHECK DB.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "perfil_usuario_id")
    private Perfil perfilUsuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "perfil_empresa_id")
    private PerfilEmpresa perfilEmpresa;

    // ---------------------------
    // TIPO Y ROL
    // ---------------------------
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_telefono", nullable = false, length = 20)
    private TipoTelefono tipoTelefono;

    @Column(length = 100)
    private String etiqueta; // Ej: "Soporte", "Ventas"

    // ---------------------------
    // NÚMERO Y CÓDIGO DE ÁREA
    // ---------------------------
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "codigo_area_id")
    private CodigoArea codigoArea; // nullable, referencia a tabla existente

    @Column(length = 20)
    private String caracteristica; // fallback para prefijo

    @Column(length = 50, nullable = false)
    private String numero; // número local/abonado

    @Column(length = 64)
    private String formato; // E.164 normalizado (opcional)

    // ---------------------------
    // ESTADO Y PREFERENCIAS
    // ---------------------------
    @Column(nullable = false)
    private Boolean activo = true;

    @Column(nullable = false)
    private Boolean verificado = false;

    @Column(nullable = false)
    private Boolean favorito = false; // cada owner puede tener su favorito

    // ---------------------------
    // TRAZABILIDAD / AUDITORÍA (Spring Data JPA Auditing)
    // ---------------------------
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @LastModifiedBy
    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Version
    private Integer version;

    // ---------------------------
    // METADATOS DE COPIA / ORIGEN
    // ---------------------------
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "copiada_de_telefono_id")
    private Telefono copiadaDeTelefono; // FK a telefono.id, ON DELETE SET NULL (definir en DDL)

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private Origen origen = Origen.MANUAL;

    @Column(name = "sync_enabled", nullable = false)
    private Boolean syncEnabled = false;

    // ---------------------------
    // REGLAS/VALIDACIONES EN ENTIDAD
    // ---------------------------
    @PrePersist
    protected void prePersist() {
        if ((perfilUsuario == null) == (perfilEmpresa == null)) {
            throw new IllegalStateException("Exactly one of perfilUsuario or perfilEmpresa must be set");
        }
        // timestamps handled by AuditingEntityListener (@CreatedDate/@LastModifiedDate)
    }

    @PreUpdate
    protected void preUpdate() {
        if ((perfilUsuario == null) == (perfilEmpresa == null)) {
            throw new IllegalStateException("Exactly one of perfilUsuario or perfilEmpresa must be set");
        }
        // updatedAt handled by AuditingEntityListener (@LastModifiedDate)
    }

    public enum TipoTelefono { PRINCIPAL, SECUNDARIO, WHATSAPP, EMPRESA }
    public enum Origen { MANUAL, IMPORT, API, COPIA }
}
