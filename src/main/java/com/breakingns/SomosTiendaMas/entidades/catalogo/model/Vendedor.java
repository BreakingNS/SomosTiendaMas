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

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Column
    private Double rating;
}
