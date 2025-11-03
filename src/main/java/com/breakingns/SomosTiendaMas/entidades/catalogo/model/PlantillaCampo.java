package com.breakingns.SomosTiendaMas.entidades.catalogo.model;

import com.breakingns.SomosTiendaMas.entidades.catalogo.enums.CampoTipo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "plantilla_campo")
@Getter
@Setter
public class PlantillaCampo extends BaseEntidadAuditada {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plantilla_id", nullable = false)
    private PlantillaCategoria plantilla;

    @Column(nullable = false)
    private String nombre;       // "Pulgadas", "SmartTV"

    @Column(length = 80)
    private String slug;         // "pulgadas", "smart-tv"

    @Enumerated(EnumType.STRING)
    private CampoTipo tipo;      // TEXT, NUMBER, BOOLEAN, ENUM, DATE, JSON

    @Column(columnDefinition = "text")
    private String opcionesJson; // para ENUM/SELECT: '["32\"", "43\""]' o similar

    private Integer orden = 0;
    private boolean requerido = false;
}