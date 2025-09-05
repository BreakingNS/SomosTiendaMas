package com.breakingns.SomosTiendaMas.auth.model;

import com.breakingns.SomosTiendaMas.entidades.usuario.model.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@Table(name = "email_verificacion")
public class EmailVerificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String codigo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(nullable = false)
    private LocalDateTime fechaExpiracion;

    @Column(nullable = false)
    private Boolean usado = false;
    
    public EmailVerificacion() {}

    public EmailVerificacion(String codigo, Usuario usuario, LocalDateTime fechaExpiracion) {
        this.codigo = codigo;
        this.usuario = usuario;
        this.fechaExpiracion = fechaExpiracion;
        this.usado = false;
        this.fechaCreacion = LocalDateTime.now();
    }

    public boolean isUsado() {
        return this.usado; // Suponiendo que tienes un campo boolean usado
    }

    public LocalDateTime getFechaCreacion() {
        return this.fechaCreacion; // Suponiendo que tienes un campo LocalDateTime fechaCreacion
    }
}
