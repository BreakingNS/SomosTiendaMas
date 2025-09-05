package com.breakingns.SomosTiendaMas.entidades.usuario.model;

import com.breakingns.SomosTiendaMas.auth.model.Rol;
import com.breakingns.SomosTiendaMas.auth.model.SesionActiva;
import com.breakingns.SomosTiendaMas.entidades.direccion.model.Direccion;
import com.breakingns.SomosTiendaMas.entidades.empresa.model.PerfilEmpresa;
import com.breakingns.SomosTiendaMas.entidades.telefono.model.Telefono;
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
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
@Table(name = "usuario", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"username"}),
    @UniqueConstraint(columnNames = {"email"}),
    @UniqueConstraint(columnNames = {"responsable_documento"})
})
public class Usuario {
     
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long idUsuario;

    @Column(length = 50, nullable = false, unique = true)
    private String username;

    @Column(length = 100, nullable = false, unique = true)
    private String email;

    @Column(length = 255, nullable = false)
    private String password;
    
    @Column(nullable = false)
    private Boolean activo;

    @Column(nullable = false)
    private Boolean emailVerificado;

    @Column(nullable = false)
    private LocalDateTime fechaRegistro;

    @Column(nullable = false)
    private Integer intentosFallidosLogin;

    @Column(nullable = false)
    private Boolean cuentaBloqueada;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoUsuario tipoUsuario;
    
    @Column(length = 100, nullable = false)
    private String nombreResponsable;

    @Column(length = 100, nullable = false)
    private String apellidoResponsable;

    @Column(length = 20, nullable = false, unique = true)
    private String documentoResponsable;
    
    @Column(nullable = false)
    private Boolean aceptaTerminos;

    @Column(nullable = false)
    private Boolean aceptaPoliticaPriv;
    //---
    // Campos importantes
    private LocalDateTime fechaVerificacionEmail;
    private LocalDateTime fechaUltimoAcceso;

    @Column(nullable = false)
    private LocalDateTime fechaUltimaModificacion;

    private LocalDate fechaNacimientoResponsable;

    @Enumerated(EnumType.STRING)
    private Genero generoResponsable;

    // Campos opcionales
    private Boolean recibirPromociones;
    private Boolean recibirNewsletters;
    private Boolean notificacionesEmail;
    private Boolean notificacionesSms;

    @Column(length = 5)
    private String idioma;

    @Column(length = 50)
    private String timezone;
    
    // Relaciones con roles, sesiones y carrito
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "usuario_roles",
            joinColumns = @JoinColumn(name = "id_usuario"),
            inverseJoinColumns = @JoinColumn(name = "id_rol"))
    private Set<Rol> roles = new HashSet<>();

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SesionActiva> sesionesActivas = new ArrayList<>();

    @JsonIgnore
    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private Carrito carrito;        

    @JsonIgnore
    @OneToMany(mappedBy = "usuario")
    private List<Telefono> telefonos;

    @JsonIgnore
    @OneToMany(mappedBy = "usuario")
    private List<Direccion> direcciones;

    @JsonIgnore
    @OneToMany(mappedBy = "usuario")
    private List<PerfilEmpresa> empresas;
    public Usuario() {}

    public Usuario(Long idUsuario, String username, String password, String email, Carrito carrito) {
        this.idUsuario = idUsuario;
        this.username = username;
        this.password = password;
        this.email = email;
        this.carrito = carrito;
    }

    public enum TipoUsuario {
        PERSONA_FISICA, EMPRESA
    }

    public enum Genero {
        MASCULINO, FEMENINO, OTRO
    }

    public boolean isActivo() {
        return Boolean.TRUE.equals(this.activo);
    }

    public boolean isCuentaBloqueada() {
        return this.cuentaBloqueada != null && this.cuentaBloqueada;
    }

    public void setRol(Rol rol) {
        this.roles.clear();
        this.roles.add(rol);
    }
}
//---------------------------------------------------------------
