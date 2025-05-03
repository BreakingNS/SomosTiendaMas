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
public class TokenEmitido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 2048)
    private String token;

    @Column(nullable = false)
    private Instant fechaEmision;

    @Column(nullable = false)
    private Instant fechaExpiracion;

    @Column(nullable = false)
    private boolean revocado = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    public TokenEmitido() {
    }

    public TokenEmitido(Long id, String token, Instant fechaEmision, Instant fechaExpiracion, Usuario usuario) {
        this.id = id;
        this.token = token;
        this.fechaEmision = fechaEmision;
        this.fechaExpiracion = fechaExpiracion;
        this.usuario = usuario;
    }
    
    //Getters y Setters
}