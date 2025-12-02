package com.breakingns.SomosTiendaMas.entidades.catalogo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
// antes: @Table(name = "opcion_producto")
@Table(name = "opcion") // nombre claro y no ambigüo con producto_opcion
@Getter @Setter
public class Opcion extends BaseEntidadAuditada {
    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(name = "orden", nullable = false)
    private Integer orden = 0;

    @Column(name = "tipo", length = 60)
    private String tipo;
}