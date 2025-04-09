package com.breakingns.SomosTiendaMas.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(unique = true, nullable = false)
    private RolNombre nombre;

    public Rol() {}

    public Rol(RolNombre nombre) {
        this.nombre = nombre;
    }
}