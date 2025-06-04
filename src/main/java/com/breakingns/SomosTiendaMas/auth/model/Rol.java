package com.breakingns.SomosTiendaMas.auth.model;

import com.breakingns.SomosTiendaMas.model.RolNombre;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_rol;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, unique = true, nullable = false)
    private RolNombre nombre;

    public Rol() {}

    public Rol(RolNombre nombre) {
        this.nombre = nombre;
    }
    
    // Getters y Setters
}