package com.breakingns.SomosTiendaMas.entidades.perfil.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.breakingns.SomosTiendaMas.entidades.direccion.model.Direccion;
import com.breakingns.SomosTiendaMas.entidades.telefono.model.Telefono;
import com.breakingns.SomosTiendaMas.entidades.usuario.model.Usuario;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "perfil_usuario", indexes = {
        @Index(name = "ux_perfil_documento", columnList = "documento", unique = true),
        @Index(name = "ux_perfil_usuario", columnList = "usuario_id", unique = true)
})
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Perfil {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "perfil_seq")
    @SequenceGenerator(name = "perfil_seq", sequenceName = "perfil_id_seq", allocationSize = 1)
    private Long id;

    // ---------------------------
    // Identificación
    // ---------------------------
    // DUEÑO de la relación 1:1 (FK NOT NULL UNIQUE)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", unique = true, nullable = false)
    private Usuario usuario;

    @Column(length = 20, nullable = false, unique = true)
    @NotBlank
    @Size(max = 20)
    @JsonIgnore // PII: exponer sólo vía DTOs/endpoint autorizados
    private String documento;

    // ---------------------------
    // Nombre / Display
    // ---------------------------
    @Column(length = 100, nullable = false)
    @NotBlank
    @Size(max = 100)
    private String nombre;

    @Column(length = 100, nullable = false)
    @NotBlank
    @Size(max = 100)
    private String apellido;

    // ---------------------------
    // Contacto
    // ---------------------------
    @Column(length = 150)
    @Email
    @Size(max = 150)
    private String correoAlternativo;

    @JsonIgnore
    @OneToMany(mappedBy = "perfilUsuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Direccion> direcciones = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "perfilUsuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Telefono> telefonos = new ArrayList<>();

    // ---------------------------
    // Datos personales
    // ---------------------------
    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(length = 16)
    private String genero;

    // campos opcionales simplificados para responsable
    @Column(length = 100)
    private String cargo; // ej. "Propietario", "Gerente"

    // ---------------------------
    // Metadatos / auditoría
    // ---------------------------
    @Column
    private Boolean activo = true;

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

    
    @Column(name = "metadata_json", columnDefinition = "text")
    private String metadataJson;

    @Version
    private Integer version;
    // ---------------------------
    // Fin campos
    // ---------------------------

    // Validations and business rules can still use lifecycle callbacks if needed.
    @PrePersist
    protected void prePersist() {
        // auditing will set createdAt/createdBy/updatedAt/updatedBy
        if (this.activo == null) this.activo = true;
    }

    @PreUpdate
    protected void preUpdate() {
        // auditing will update timestamps
    }

}