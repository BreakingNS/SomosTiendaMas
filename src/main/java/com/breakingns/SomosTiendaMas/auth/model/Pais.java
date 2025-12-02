package com.breakingns.SomosTiendaMas.auth.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pais")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode(of = "id")
public class Pais {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, unique = true)
    private String nombre;

    public Pais(String nombre) {
        this.nombre = nombre;
    }
}