package com.breakingns.SomosTiendaMas.entidades.catalogo.model;

import com.breakingns.SomosTiendaMas.entidades.catalogo.enums.Moneda;
import com.breakingns.SomosTiendaMas.entidades.catalogo.enums.TipoPrecio;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "precio_variante", indexes = {
        @Index(name = "ix_precio_variante_variante", columnList = "variante_id"),
        @Index(name = "ix_precio_vigencia", columnList = "vigencia_desde, vigencia_hasta")
})
@Getter
@Setter
public class PrecioVariante extends BaseEntidadAuditada {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "variante_id", nullable = false)
    private VarianteProducto variante;

    @Column(name = "monto_centavos", nullable = false)
    private Long montoCentavos;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private Moneda moneda = Moneda.ARS;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TipoPrecio tipo = TipoPrecio.LISTA;

    @Column(name = "vigencia_desde")
    private LocalDateTime vigenciaDesde;

    @Column(name = "vigencia_hasta")
    private LocalDateTime vigenciaHasta;

    @Column(nullable = false)
    private Boolean activo = true;
}
