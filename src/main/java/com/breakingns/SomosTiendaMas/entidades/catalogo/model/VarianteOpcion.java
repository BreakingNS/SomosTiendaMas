package com.breakingns.SomosTiendaMas.entidades.catalogo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "variante_opcion", uniqueConstraints = {
        @UniqueConstraint(name = "ux_variante_opcion", columnNames = {"variante_id", "opcion_id"})
}, indexes = {
        @Index(name = "ix_variante_opcion_variante", columnList = "variante_id"),
        @Index(name = "ix_variante_opcion_opcion", columnList = "opcion_id")
})
@Getter
@Setter
public class VarianteOpcion extends BaseEntidadAuditada {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "variante_id", nullable = false)
    private Variante variante;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "opcion_id", nullable = false)
    private Opcion opcion;

    @Column(name = "orden", nullable = false)
    private Integer orden = 0;

    @Column(name = "requerido", nullable = false)
    private boolean requerido = false;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    @Column(name = "metadata_json", columnDefinition = "TEXT")
    private String metadataJson;

}
