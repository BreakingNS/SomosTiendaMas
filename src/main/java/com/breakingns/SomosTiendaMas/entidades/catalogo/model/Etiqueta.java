package com.breakingns.SomosTiendaMas.entidades.catalogo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "etiqueta", indexes = {
        @Index(name = "ux_etiqueta_slug", columnList = "slug", unique = true)
})
@Getter
@Setter
public class Etiqueta extends BaseEntidadAuditada {

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, length = 160, unique = true)
    private String slug;
}
