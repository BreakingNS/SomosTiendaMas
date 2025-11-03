// VISTO BUENO
package com.breakingns.SomosTiendaMas.entidades.catalogo.model;

import com.breakingns.SomosTiendaMas.entidades.catalogo.enums.Moneda;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "precio_producto", indexes = {
        @Index(name = "ix_precio_producto_producto", columnList = "producto_id"),
        @Index(name = "ix_precio_producto_vigencia", columnList = "vigencia_desde, vigencia_hasta")
})
@Getter
@Setter
public class PrecioProducto extends BaseEntidadAuditada {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(name = "monto_centavos", nullable = false)
    private Long montoCentavos;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private Moneda moneda = Moneda.ARS;

    @Column(name = "vigencia_desde")
    private LocalDateTime vigenciaDesde;

    @Column(name = "vigencia_hasta")
    private LocalDateTime vigenciaHasta;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "creado_por", length = 120)
    private String creadoPor;
}
