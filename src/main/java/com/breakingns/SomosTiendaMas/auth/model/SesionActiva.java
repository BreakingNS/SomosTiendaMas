package com.breakingns.SomosTiendaMas.auth.model;

import com.breakingns.SomosTiendaMas.domain.usuario.model.Usuario;
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
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
@Table(name = "sesiones_activas")
public class SesionActiva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 512)
    private String token;

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
    
    public SesionActiva(Long id, String token, String ip, String userAgent, Usuario usuario, Instant fechaInicioSesion, Instant fechaExpiracion) {
        this.id = id;
        this.token = token;
        this.ip = ip;
        this.userAgent = userAgent;
        this.usuario = usuario;
        this.fechaInicioSesion = fechaInicioSesion;
        this.fechaExpiracion = fechaExpiracion;
    }

    // Getters, setters, toString, etc.
    
}