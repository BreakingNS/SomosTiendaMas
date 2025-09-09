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
    private Instant fechaCreacion = Instant.now();

    @Column(nullable = false)
    private Instant fechaExpiracion;

    @Column(nullable = false)
    private boolean usado = false;

    // Constructor vacío obligatorio
    public TokenResetPassword() {}

    public TokenResetPassword(String token, Usuario usuario, Instant fechaExpiracion) {
        this.token = token;
        this.usuario = usuario;
        this.fechaCreacion = Instant.now();
        this.fechaExpiracion = fechaExpiracion;
        this.usado = false;
    }

    // Genera un token alfanumérico de longitud dada
    public static String generarTokenAlfanumerico(int longitud) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+[]{}|;:,.<>?";
        StringBuilder sb = new StringBuilder();
        java.security.SecureRandom random = new java.security.SecureRandom();
        for (int i = 0; i < longitud; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    // Getters y setters

    public boolean isExpirado() {
        return Instant.now().isAfter(this.fechaExpiracion);
    }
}