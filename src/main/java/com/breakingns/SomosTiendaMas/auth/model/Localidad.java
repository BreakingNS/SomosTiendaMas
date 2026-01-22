package com.breakingns.SomosTiendaMas.auth.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "dir_localidades")
@Getter @Setter
public class Localidad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;

    @ManyToOne
    private Municipio municipio;

    @ManyToOne
    private Departamento departamento;

    @ManyToOne
    private Provincia provincia;
}
