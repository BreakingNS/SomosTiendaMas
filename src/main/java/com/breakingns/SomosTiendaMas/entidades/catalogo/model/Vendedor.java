package com.breakingns.SomosTiendaMas.entidades.catalogo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "vendedor", indexes = {
        @Index(name = "ix_vendedor_user", columnList = "user_id")
})
@Getter
@Setter
public class Vendedor extends BaseEntidadAuditada {

    // -----------------------------
    // Relación con usuario / identificador de cuenta
    // -----------------------------
    // Referencia al usuario (cuenta) que administra este vendedor
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // -----------------------------
    // Datos públicos del vendedor / tienda
    // -----------------------------
    // Nombre público del vendedor o tienda
    @Column(nullable = false, length = 200)
    private String nombre;

    // Descripción pública del vendedor/tienda
    @Column(length = 500)
    private String descripcion;

    // -----------------------------
    // Metadatos operativos
    // -----------------------------
    // Rating o reputación agregada
    @Column
    private Double rating;

    // Estado del vendedor (activo/inactivo) — útil para suspensiones o controles
    @Column(name = "activo", nullable = false)
    private boolean activo = true;
}
