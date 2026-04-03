package com.breakingns.SomosTiendaMas.entidades.direccion.model;

import com.breakingns.SomosTiendaMas.entidades.perfil.model.Perfil;
import com.breakingns.SomosTiendaMas.entidades.perfil_empresa.model.PerfilEmpresa;
import com.breakingns.SomosTiendaMas.auth.model.*;
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
@Table(name = "direccion",
       indexes = {
           @Index(name = "ix_direccion_perfil_usuario", columnList = "perfil_usuario_id"),
           @Index(name = "ix_direccion_perfil_empresa", columnList = "perfil_empresa_id"),
           @Index(name = "ix_direccion_copied_from", columnList = "copied_from_direccion_id")
       }
)
/**
 * Entidad `Direccion` — entidad unificada para almacenar direcciones de
 * usuarios (`Perfil`) y empresas (`PerfilEmpresa`).
 *
 * Contiene:
 * - Identidad/propietario: referencia a `perfilUsuario` o `perfilEmpresa` (XOR).
 * - Tipo/uso: enum `TipoDireccion`, flags `esPrincipal` y `activa`.
 * - Componentes: pais/provincia/departamento/localidad/municipio, calle, numero,
 *   piso, departamentoInterno, codigoPostal, referencia y notas.
 * - Trazabilidad/auditoría: `createdAt`, `createdBy`, `updatedAt`, `updatedBy`,
 *   `deletedAt` y callbacks `@PrePersist`/`@PreUpdate`.
 * - Control de concurrencia: campo `@Version` para optimistic locking.
 * - Metadatos de copia/origen: `copiedFromDireccion`, `canonicalAddressId`,
 *   `origin`, `originOwnerType`, `originOwnerId`, `syncEnabled`.
 */
public class Direccion {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "direccion_seq")
    @SequenceGenerator(name = "direccion_seq", sequenceName = "direccion_id_seq", allocationSize = 1)
    private Long id;

    // ---------------------------
    // Identidad / propietario
    // ---------------------------
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "perfil_usuario_id")
    private Perfil perfilUsuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "perfil_empresa_id")
    private PerfilEmpresa perfilEmpresa;

    // REVIEW: exactamente uno de `perfilUsuario` o `perfilEmpresa` debe estar seteado (XOR).
    // Validar en capa servicio o con CHECK DB si el motor lo soporta.

    // ---------------------------
    // Tipo y uso
    // ---------------------------
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoDireccion tipo;

    @Column(nullable = false)
    private Boolean esPrincipal = false;

    @Column(nullable = false)
    private Boolean activa = true;

    // ---------------------------
    // Componentes de dirección
    // ---------------------------
    @ManyToOne @JoinColumn(name = "id_pais", nullable = false)
    private Pais pais;

    @ManyToOne @JoinColumn(name = "id_provincia", nullable = false)
    private Provincia provincia;

    @ManyToOne @JoinColumn(name = "id_departamento", nullable = false)
    private Departamento departamento;

    @ManyToOne @JoinColumn(name = "id_localidad", nullable = false)
    private Localidad localidad;

    @ManyToOne @JoinColumn(name = "id_municipio", nullable = false)
    private Municipio municipio;

    @Column(length = 200, nullable = false)
    private String calle;

    @Column(length = 20, nullable = false)
    private String numero;

    @Column(length = 20)
    private String piso;

    @Column(length = 20)
    private String departamentoInterno;

    @Column(length = 20, nullable = false)
    private String codigoPostal;

    @Column(columnDefinition = "TEXT")
    private String referencia;

    @Column(columnDefinition = "TEXT")
    private String notas;

    // ---------------------------
    // Trazabilidad / auditoría (Spring Data JPA Auditing)
    // ---------------------------
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @LastModifiedBy
    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Version
    private Integer version;

    // ---------------------------
    // Metadatos de duplicado / origen
    // ---------------------------
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "copied_from_direccion_id")
    private Direccion copiedFromDireccion;

    @Column(name = "canonical_address_id")
    private Long canonicalAddressId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Origen origen = Origen.MANUAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "origin_owner_type", length = 10)
    private OriginOwnerType originOwnerType;

    @Column(name = "origin_owner_id")
    private Long originOwnerId;

    @Column(name = "sync_enabled", nullable = false)
    private Boolean syncEnabled = false;

    // ---------------------------
    // Callbacks
    // ---------------------------
    // Validation hooks (no timestamp management here — Auditing handles dates)

    // ---------------------------
    // Enums
    // ---------------------------
    public enum TipoDireccion { PERSONAL, FISCAL, ENVIO, FACTURACION }
    public enum Origen { MANUAL, IMPORT, API, COPIA }
    public enum OriginOwnerType { USUARIO, EMPRESA }
}
