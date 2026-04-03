package com.breakingns.SomosTiendaMas.entidades.perfil_empresa.model;

import com.breakingns.SomosTiendaMas.entidades.direccion.model.Direccion;
import com.breakingns.SomosTiendaMas.entidades.perfil.model.Perfil;
import com.breakingns.SomosTiendaMas.entidades.telefono.model.Telefono;
import com.breakingns.SomosTiendaMas.entidades.usuario.model.Usuario;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Getter;
import lombok.Setter;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
@Table(name = "perfil_empresa",
       uniqueConstraints = { @UniqueConstraint(columnNames = {"cuit"}) },
       indexes = {
           @Index(name = "idx_perfil_empresa_usuario", columnList = "id_usuario"),
           @Index(name = "idx_perfil_empresa_estado", columnList = "estado_aprobado"),
           @Index(name = "idx_perfil_empresa_activo", columnList = "activo")
       }
)
public class PerfilEmpresa {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "perfil_empresa_seq")
    @SequenceGenerator(name = "perfil_empresa_seq", sequenceName = "perfil_empresa_id_seq", allocationSize = 1)
    private Long idPerfilEmpresa;

    // ---------------------------
    // Identificación
    // ---------------------------
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_perfil_id", unique = true)
    private Perfil responsable;

    @Column(name = "razon_social", length = 200, nullable = false)
    @NotBlank
    @Size(max = 200)
    private String razonSocial;

    @Column(name = "nombre_comercial", length = 200)
    @Size(max = 200)
    private String nombreComercial;

    @Column(length = 15, nullable = false, unique = true)
    @NotBlank
    @Size(max = 15)
    @Pattern(regexp = "^[0-9-]{7,15}$", message = "CUIT inválido")
    @JsonIgnore
    private String cuit;

    // ---------------------------
    // Fiscal / legal
    // ---------------------------
    @Enumerated(EnumType.STRING)
    @Column(name = "condicion_iva", nullable = false)
    private CondicionIVA condicionIVA;

    @Column(name = "requiere_facturacion", nullable = false)
    private Boolean requiereFacturacion = true;

    // ---------------------------
    // Estado / workflow
    // ---------------------------
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_aprobado", nullable = false)
    private EstadoAprobado estadoAprobado;

    // ---------------------------
    // Contacto / comercial
    // ---------------------------
    @Column(name = "email_empresa", length = 100, nullable = false)
    @NotBlank
    @Email
    @Size(max = 100)
    private String emailEmpresa;

    // Campos importantes
    @Column(columnDefinition = "TEXT")
    private String descripcionEmpresa;

    @Column(length = 255)
    @Size(max = 255)
    private String sitioWeb;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria_empresa")
    private CategoriaEmpresa categoriaEmpresa;

    @Column(precision = 12, scale = 2)
    private BigDecimal limiteCreditoVentas;

    // ---------------------------
    // Finanzas / operaciones
    // ---------------------------
    private Integer tiempoProcesamientoPedidos;

    // ---------------------------
    // Metadatos / auditoría
    // ---------------------------
    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    @LastModifiedDate
    @Column(name = "fecha_ultima_modificacion")
    private LocalDateTime fechaUltimaModificacion;

    @LastModifiedBy
    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "metadata_json", columnDefinition = "text")
    private String metadataJson;

    @Version
    private Integer version;

    // Campos opcionales
    // ---------------------------
    // Branding / presentación
    // ---------------------------
    @Column(length = 500)
    private String logoUrl;

    @Column(length = 7)
    private String colorCorporativo;

    @Column(length = 200)
    private String descripcionCorta;

    @Column(length = 100)
    private String horarioAtencion;

    @Column(length = 50)
    private String diasLaborales;

    // ---------------------------
    // Contacto / relaciones
    // ---------------------------
    @JsonIgnore
    @OneToMany(mappedBy = "perfilEmpresa", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Telefono> telefonos = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "perfilEmpresa", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Direccion> direcciones = new ArrayList<>();

    // ---------------------------
    // Enums
    // ---------------------------
    public enum CondicionIVA { RI, MONOTRIBUTO, EXENTO }
    public enum EstadoAprobado { PENDIENTE, APROBADO, RECHAZADO }
    public enum CategoriaEmpresa { RETAIL, MAYORISTA, FABRICANTE }
    @PrePersist
    protected void prePersist() {
        if (this.activo == null) this.activo = true;
        // fechaCreacion/fechaUltimaModificacion handled by Auditing
    }

    @PreUpdate
    protected void preUpdate() {
        // fechaUltimaModificacion handled by Auditing
    }
}
