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
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private Instant fechaExpiracion;

    @Column(nullable = false)
    private Boolean usado = false;

    @Column(nullable = false)
    private Boolean revocado = false;

    private String ip;
    private String userAgent;

    // Getters y Setters

    public RefreshToken() {
    }

    public RefreshToken(Long id, String token, Usuario usuario, Instant fechaExpiracion, String ip, String userAgent) {
        this.id = id;
        this.token = token;
        this.usuario = usuario;
        this.fechaExpiracion = fechaExpiracion;
        this.ip = ip;
        this.userAgent = userAgent;
    }

    

}

