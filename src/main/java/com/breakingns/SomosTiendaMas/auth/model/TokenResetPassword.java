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
@Table(name = "tokens_reset_password")
public class TokenResetPassword {

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
    private boolean usado = false;

    // Constructor vacío obligatorio
    public TokenResetPassword() {}

    public TokenResetPassword(String token, Usuario usuario, Instant fechaExpiracion) {
        this.token = token;
        this.usuario = usuario;
        this.fechaExpiracion = fechaExpiracion;
        this.usado = false;
    }

    // Getters y setters

    public boolean isExpirado() {
        return Instant.now().isAfter(this.fechaExpiracion);
    }
}