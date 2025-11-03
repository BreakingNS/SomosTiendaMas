package com.breakingns.SomosTiendaMas.entidades.catalogo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "producto_atributo")
@Getter
@Setter
public class ProductoAtributo extends BaseEntidadAuditada {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    // si instancias desde plantilla:
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plantilla_campo_id")
    private PlantillaCampo plantillaCampo;

    @Column(nullable = false)
    private String nombre;       // copia del nombre (para historial)

    @Column(length = 80)
    private String slug;

    // valor guardado siempre como texto; parsear según tipo
    @Column(columnDefinition = "text")
    private String valor;
}