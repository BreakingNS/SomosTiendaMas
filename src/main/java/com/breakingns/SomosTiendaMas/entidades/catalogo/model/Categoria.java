package com.breakingns.SomosTiendaMas.entidades.catalogo.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categoria", indexes = {
        @Index(name = "ux_categoria_slug", columnList = "slug", unique = true),
        @Index(name = "idx_categoria_padre", columnList = "categoria_padre_id")
})
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Categoria extends BaseEntidadAuditada {

    @Column(nullable = false, length = 160)
    @ToString.Include
    private String nombre;

    @Column(nullable = false, length = 180, unique = true)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_padre_id")
    @ToString.Exclude
    private Categoria categoriaPadre;

    // Opcional: colección de hijos para facilitar navegación en memoria (no necesario si preferís queries)
    @OneToMany(mappedBy = "categoriaPadre", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Categoria> hijos = new ArrayList<>();

    // ...existing code...
}