package com.breakingns.SomosTiendaMas.entidades.catalogo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "variante_opcion_valor", uniqueConstraints = {
        @UniqueConstraint(name = "ux_variante_opcion_valor", columnNames = {"variante_id", "opcion_valor_id"}),
        @UniqueConstraint(name = "ux_variante_opcion", columnNames = {"variante_id", "opcion_id"})
}, indexes = {
        @Index(name = "ix_vov_variante", columnList = "variante_id"),
        @Index(name = "ix_vov_opcion_valor", columnList = "opcion_valor_id"),
        @Index(name = "ix_vov_opcion", columnList = "opcion_id")
})
@Getter
@Setter
public class VarianteOpcionValor extends BaseEntidadAuditada {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "variante_id", nullable = false)
    private Variante variante;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "opcion_valor_id", nullable = false)
    private OpcionValor opcionValor;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "opcion_id", nullable = false)
        private Opcion opcion;

    // metadata/overrides específicos de la asociación
    @Column(name = "metadata_json", columnDefinition = "TEXT")
    private String metadataJson;

}
