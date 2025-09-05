package com.breakingns.SomosTiendaMas.auth.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

import com.breakingns.SomosTiendaMas.entidades.usuario.model.Usuario;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
@Table(name = "sesiones_activas")
public class SesionActiva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 2048)
    private String token;

    // NUEVO CAMPO: refreshToken asociado a la sesión
    @Column(nullable = true, length = 2048)
    private String refreshToken;

    @Column(nullable = false)
    private String ip;

    @Column(nullable = false)
    private String userAgent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private Instant fechaInicioSesion;

    @Column
    private Instant fechaExpiracion;

    @Column(nullable = false)
    private boolean revocado = false;

    public SesionActiva() {
    }
    
    public SesionActiva(Long id, String token, String refreshToken, String ip, String userAgent, Usuario usuario, Instant fechaInicioSesion, Instant fechaExpiracion) {
        this.id = id;
        this.token = token;
        this.refreshToken = refreshToken;
        this.ip = ip;
        this.userAgent = userAgent;
        this.usuario = usuario;
        this.fechaInicioSesion = fechaInicioSesion;
        this.fechaExpiracion = fechaExpiracion;
    }

    // Getters, setters, toString, etc.
    
}