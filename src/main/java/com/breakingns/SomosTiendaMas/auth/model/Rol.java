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

    @Column(length = 255)
    private String descripcion;

    public Rol() {}

    public Rol(RolNombre nombre) {
        this.nombre = nombre;
    }

    public Rol(Long id, RolNombre nombre, String descripcion) {
        this.id_rol = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public Object stream() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'stream'");
    }
    
    // Getters y Setters (los genera Lombok)
}