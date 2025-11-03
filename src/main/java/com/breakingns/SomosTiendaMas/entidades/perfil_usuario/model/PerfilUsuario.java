package com.breakingns.SomosTiendaMas.entidades.perfil_usuario.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.breakingns.SomosTiendaMas.entidades.direccion.model.DireccionUsuario;
import com.breakingns.SomosTiendaMas.entidades.telefono.model.TelefonoUsuario;
import com.breakingns.SomosTiendaMas.entidades.usuario.model.Usuario;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "perfil_usuario", indexes = {
        @Index(name = "ux_perfil_documento", columnList = "documento", unique = true),
        @Index(name = "ux_perfil_usuario", columnList = "usuario_id", unique = true)
})
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class PerfilUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    // DUEÑO de la relación 1:1 (FK UNIQUE NOT NULL)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", unique = true, nullable = false)
    private Usuario usuario;

    @JsonIgnore
    @OneToMany(mappedBy = "perfilUsuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DireccionUsuario> direcciones = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "perfilUsuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TelefonoUsuario> telefonos = new ArrayList<>();

    @Column(length = 100, nullable = false)
    private String nombre;

    @Column(length = 100, nullable = false)
    private String apellido;

    @Column(length = 20, nullable = false, unique = true)
    private String documento;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(length = 16)
    private String genero;

    // campos opcionales simplificados para responsable
    @Column(length = 100)
    private String cargo;          // ej. "Propietario", "Gerente"

    @Column(length = 150)
    private String correoAlternativo;

}