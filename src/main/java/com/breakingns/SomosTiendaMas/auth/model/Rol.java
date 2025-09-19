package com.breakingns.SomosTiendaMas.auth.model;

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
    @Column(nullable = false)
    private RolNombre nombre;

    public Rol() {}

    public Rol(RolNombre nombre) {
        this.nombre = nombre;
    }

    public Object stream() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'stream'");
    }
    
    // Getters y Setters
}