package com.breakingns.SomosTiendaMas.entidades.catalogo.model;

import java.util.ArrayList;
import java.util.List;

import com.breakingns.SomosTiendaMas.entidades.catalogo.converter.EstadoModeracionConverter;
import com.breakingns.SomosTiendaMas.entidades.catalogo.enums.EstadoModeracion;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "marca", indexes = {
        @Index(name = "ux_marca_slug", columnList = "slug", unique = true),
        @Index(name = "ix_marca_estado_moderacion", columnList = "estado_moderacion")
})
@Getter
@Setter
public class Marca extends BaseEntidadAuditada {

    // -----------------------------
    // Metadatos básicos
    // -----------------------------
    @Column(nullable = false, length = 160)
    private String nombre;

    @Column(nullable = false, length = 180, unique = true)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    // -----------------------------
    // Relaciones
    // -----------------------------
    // Relación 1:N hacia Producto (no usar cascade REMOVE para evitar borrados en cascada)
    @OneToMany(mappedBy = "marca", fetch = FetchType.LAZY)
    private List<Producto> productos = new ArrayList<>();

    // Marcas aplicables a categorías específicas (p.ej. Makita -> Herramientas)
    @ManyToMany
    @JoinTable(name = "marca_categoria",
        joinColumns = @JoinColumn(name = "marca_id"),
        inverseJoinColumns = @JoinColumn(name = "categoria_id")
    )
    private List<Categoria> categorias = new ArrayList<>();

    // -----------------------------
    // Moderación y metadatos administrativos
    // -----------------------------
    // Marca creada por usuario y pendiente de moderación
    @Column(name = "creada_por_usuario", nullable = false)
    private boolean creadaPorUsuario = false;

    // referencia opcional al vendedor que la creó (si aplica)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creada_por_vendedor_id")
    private Vendedor creadaPor;

    @Convert(converter = EstadoModeracionConverter.class)
    @Column(name = "estado_moderacion", length = 32, nullable = false)
    private EstadoModeracion estadoModeracion = EstadoModeracion.PENDIENTE;

    @Column(name = "moderacion_notas", columnDefinition = "TEXT")
    private String moderacionNotas;

    // datos simples del moderador; si tenés entidad de usuario/admin podés usar referencia
    @Column(name = "moderado_por", length = 160)
    private String moderadoPor;

    @Column(name = "moderado_en")
    private LocalDateTime moderadoEn;

}