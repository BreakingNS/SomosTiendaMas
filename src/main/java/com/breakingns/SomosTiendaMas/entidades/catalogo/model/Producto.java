package com.breakingns.SomosTiendaMas.entidades.catalogo.model;

import com.breakingns.SomosTiendaMas.entidades.catalogo.converter.EstadoModeracionConverter;
import com.breakingns.SomosTiendaMas.entidades.catalogo.converter.VisibilidadProductoConverter;
import com.breakingns.SomosTiendaMas.entidades.catalogo.converter.EstadoProductoConverter;
import com.breakingns.SomosTiendaMas.entidades.catalogo.converter.CondicionProductoConverter;
import com.breakingns.SomosTiendaMas.entidades.catalogo.enums.CondicionProducto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.enums.EstadoModeracion;
import com.breakingns.SomosTiendaMas.entidades.catalogo.enums.EstadoProducto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.enums.VisibilidadProducto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "producto", indexes = {
        @Index(name = "ux_producto_slug", columnList = "slug", unique = true)
})
@Getter
@Setter
public class Producto extends BaseEntidadAuditada {
    // -----------------------------
    // Metadatos básicos (identificación)
    // -----------------------------
    @Column(nullable = false, length = 200)
    private String nombre; //LISTO

    // Slug canónico del producto (URL amigable, único)
    @Column(nullable = false, length = 220, unique = true)
    private String slug; //LISTO
    
    // -----------------------------
    // Contenido descriptivo
    // -----------------------------
    @Column(columnDefinition = "TEXT")
    private String descripcion; //LISTO

    // -----------------------------
    // Políticas comerciales y legales
    // -----------------------------
    // Garantía: texto libre que se mostrará en la ficha del producto
    @Column(name = "garantia", columnDefinition = "TEXT")
    private String garantia; //LISTO 

    // Política de devoluciones: texto libre (puede incluir condiciones, plazos, etc.)
    @Column(name = "politica_devoluciones", columnDefinition = "TEXT")
    private String politicaDevoluciones; //LISTO

    // -----------------------------
    // Relaciones (referencias a catálogos)
    // -----------------------------
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "marca_id")
    private Marca marca; //LISTO
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria; //LISTO
    
    // -----------------------------
    // Estado y visibilidad
    // -----------------------------
    @Convert(converter = EstadoModeracionConverter.class)
    @Column(name = "estado_moderacion", length = 32, nullable = false)
    private EstadoModeracion estadoModeracion = EstadoModeracion.PENDIENTE;

    @Convert(converter = EstadoProductoConverter.class)
    @Column(name = "estado_producto", nullable = false, length = 32)
    private EstadoProducto estadoProducto = EstadoProducto.BORRADOR;

    @Convert(converter = VisibilidadProductoConverter.class)
    @Column(nullable = false, length = 32)
    private VisibilidadProducto visibilidad = VisibilidadProducto.PUBLICO;

    @Convert(converter = CondicionProductoConverter.class)
    @Column(nullable = false, length = 32)
    private CondicionProducto condicion = CondicionProducto.NUEVO;

    // -----------------------------
    // Identificadores e integraciones
    // -----------------------------
    // SKU histórico (migrar a Variante; se mantiene temporalmente para compatibilidad)
    @Column(name = "sku", length = 80)
    private String sku; // opcional, no único por ahora (deshabilitado/soft)

    // -----------------------------
    // Opciones, etiquetas y atributos estructurados
    // -----------------------------
    @OneToMany(mappedBy = "producto", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = false)
    private List<ProductoOpcion> opciones = new ArrayList<>();

    @OneToMany(mappedBy = "producto", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = false)
    private List<ProductoEtiqueta> etiquetas = new ArrayList<>();

    // Variantes del producto (una o muchas)
    @OneToMany(mappedBy = "producto", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = false)
    private List<Variante> variantes = new ArrayList<>();

    public void addVariante(Variante variante) {
        variantes.add(variante);
        variante.setProducto(this);
    }

    public void removeVariante(Variante variante) {
        variantes.remove(variante);
        variante.setProducto(null);
    }

    // -----------------------------
    // Campos libres / metadata
    // -----------------------------
    // Atributos libres (JSON por categoría)
    @Column(name = "atributos_json", columnDefinition = "TEXT")
    private String atributosJson;

    // Atributos libres (JSON como TEXT por ahora)
    @Column(name = "metadata_json", columnDefinition = "TEXT")
    private String metadataJson;
    
}
