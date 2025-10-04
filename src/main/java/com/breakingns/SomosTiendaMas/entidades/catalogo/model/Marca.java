package com.breakingns.SomosTiendaMas.entidades.catalogo.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "marca", indexes = {
        @Index(name = "ux_marca_slug", columnList = "slug", unique = true)
})
@Getter
@Setter
public class Marca extends BaseEntidadAuditada {

    @Column(nullable = false, length = 160)
    private String nombre;

    @Column(nullable = false, length = 180, unique = true)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    // Relación 1:N hacia Producto (no usar cascade REMOVE para evitar borrados en cascada)
    @OneToMany(mappedBy = "marca", fetch = FetchType.LAZY)
    private List<Producto> productos = new ArrayList<>();
}
