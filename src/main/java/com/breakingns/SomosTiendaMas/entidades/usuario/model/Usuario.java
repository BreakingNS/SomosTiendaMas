package com.breakingns.SomosTiendaMas.entidades.usuario.model;

import com.breakingns.SomosTiendaMas.auth.model.EmailVerificacion;
import com.breakingns.SomosTiendaMas.auth.model.RefreshToken;
import com.breakingns.SomosTiendaMas.auth.model.Rol;
import com.breakingns.SomosTiendaMas.auth.model.SesionActiva;
import com.breakingns.SomosTiendaMas.auth.model.TokenEmitido;
import com.breakingns.SomosTiendaMas.entidades.perfil.model.Perfil;
import com.breakingns.SomosTiendaMas.entidades.perfil_empresa.model.PerfilEmpresa;
import com.breakingns.SomosTiendaMas.model.Carrito;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import jakarta.persistence.EntityListeners;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
@Table(name = "usuario", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"username"}),
    @UniqueConstraint(columnNames = {"email"})
})
public class Usuario {
     
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "usuario_seq")
    @SequenceGenerator(name = "usuario_seq", sequenceName = "usuario_id_seq", allocationSize = 1)
    private Long idUsuario;

    // --------------------------------------------------
    // AUTENTICACION BASICA
    // --------------------------------------------------
    @Column(length = 50, nullable = false, unique = true)
    private String username;

    @Column(length = 100, nullable = false, unique = true)
    private String email;

    @JsonIgnore
    @Column(length = 255, nullable = false)
    private String password;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_rol")
    private Rol rol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoUsuario tipoUsuario;

    // --------------------------------------------------
    // ESTADO/SEGURIDAD
    // --------------------------------------------------
    @Column(nullable = false)
    private Boolean activo = true;

    @Column(nullable = false)
    private Boolean cuentaBloqueada = false;

    @Column(nullable = false)
    private Boolean emailVerificado = false;

    @Column(nullable = false)
    private Integer intentosFallidosLogin = 0;

    // --------------------------------------------------
    // TIMESTAMPS/METADATA (Spring Data JPA Auditing)
    // --------------------------------------------------
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    private LocalDateTime fechaUltimoAcceso;

    private LocalDateTime fechaVerificacionEmail;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime fechaUltimaModificacion;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    @LastModifiedBy
    @Column(name = "updated_by")
    private Long updatedBy;

    @Version
    private Integer version;

    // --------------------------------------------------
    // PREFERENCIAS
    // --------------------------------------------------
    private Boolean notificacionesEmail;
    private Boolean recibirPromociones;
    private Boolean recibirNewsletters;
    private Boolean notificacionesSms;

    @Column(length = 5)
    private String idioma;

    @Column(length = 50)
    private String timezone;

    // --------------------------------------------------
    // CONSENTIMIENTO/FLAGS
    // --------------------------------------------------
    @Column(nullable = false)
    private Boolean aceptaTerminos = false;

    @Column(nullable = false)
    private Boolean aceptaPoliticaPriv = false;
    
    // Relaciones con sesiones, carrito y colecciones sensibles
    @OneToMany(mappedBy = "usuario", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<SesionActiva> sesionesActivas = new ArrayList<>();

    @JsonIgnore
    @OneToOne(mappedBy = "usuario", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Carrito carrito;

    // Empresas que administra este usuario (relación existente)
    @JsonIgnore
    @OneToMany(mappedBy = "usuario", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PerfilEmpresa> empresas = new ArrayList<>();

    @JsonIgnore
    @OneToOne(mappedBy = "usuario", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Perfil perfilUsuario;

    @JsonIgnore
    @OneToMany(mappedBy = "usuario", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmailVerificacion> emailVerificaciones = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "usuario", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RefreshToken> refreshTokens = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "usuario", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TokenEmitido> tokenEmitidos = new ArrayList<>();

    public Usuario() {}

    public Usuario(Long idUsuario, String username, String password, String email, Carrito carrito) {
        this.idUsuario = idUsuario;
        this.username = username;
        this.password = password;
        this.email = email;
        this.carrito = carrito;
    }

    public enum TipoUsuario {
        PERSONA_FISICA, EMPRESA, ADMINISTRADOR
    }

    public boolean isActivo() {
        return Boolean.TRUE.equals(this.activo);
    }

    public boolean isCuentaBloqueada() {
        return this.cuentaBloqueada != null && this.cuentaBloqueada;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    // lifecycle validations (timestamps handled by auditing)
}
//---------------------------------------------------------------
//---------------------------------------------------------------
