package com.breakingns.SomosTiendaMas.entidades.catalogo.mapper;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.precio.PrecioVarianteCrearDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.precio.PrecioVarianteActualizarDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.precio.PrecioVarianteResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.precio.PrecioVarianteResumenDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.PrecioVariante;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Producto;

import java.util.List;
import java.util.stream.Collectors;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class PrecioProductoMapper {

    private PrecioProductoMapper() {}

    public static PrecioVarianteResponseDTO toResponse(PrecioVariante p) {
        if (p == null) return null;

        BigDecimal descuento = null;
        if (p.getPrecioAnteriorCentavos() != null && p.getMontoCentavos() != null
                && p.getPrecioAnteriorCentavos() > p.getMontoCentavos()
                && p.getPrecioAnteriorCentavos() > 0) {
            BigDecimal prev = BigDecimal.valueOf(p.getPrecioAnteriorCentavos());
            BigDecimal cur = BigDecimal.valueOf(p.getMontoCentavos());
            descuento = prev.subtract(cur)
                    .divide(prev, 8, RoundingMode.HALF_EVEN)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_EVEN);
        }

        return PrecioVarianteResponseDTO.builder()
                .id(p.getId())
                .productoId(p.getProducto() != null ? p.getProducto().getId() : null)
                .montoCentavos(p.getMontoCentavos())
                .precioAnteriorCentavos(p.getPrecioAnteriorCentavos())
                .precioSinIvaCentavos(p.getPrecioSinIvaCentavos())
                .ivaPorcentaje(p.getIvaPorcentaje())
                .descuentoPorcentaje(descuento)
                .moneda(p.getMoneda())
                .vigenciaDesde(p.getVigenciaDesde())
                .vigenciaHasta(p.getVigenciaHasta())
                .activo(p.getActivo())
                .creadoPor(p.getCreadoPor())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .deletedAt(p.getDeletedAt())
                .build();
    }

    public static PrecioVarianteResumenDTO toResumen(PrecioVariante p) {
        if (p == null) return null;

        BigDecimal descuento = null;
        if (p.getPrecioAnteriorCentavos() != null && p.getMontoCentavos() != null
                && p.getPrecioAnteriorCentavos() > p.getMontoCentavos()
                && p.getPrecioAnteriorCentavos() > 0) {
            BigDecimal prev = BigDecimal.valueOf(p.getPrecioAnteriorCentavos());
            BigDecimal cur = BigDecimal.valueOf(p.getMontoCentavos());
            descuento = prev.subtract(cur)
                    .divide(prev, 8, RoundingMode.HALF_EVEN)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_EVEN);
        }

        PrecioVarianteResumenDTO r = new PrecioVarianteResumenDTO();
        r.setId(p.getId());
        r.setProductoId(p.getProducto() != null ? p.getProducto().getId() : null);
        r.setMontoCentavos(p.getMontoCentavos());
        r.setPrecioAnteriorCentavos(p.getPrecioAnteriorCentavos());
        r.setDescuentoPorcentaje(descuento);
        r.setMoneda(p.getMoneda());
        r.setActivo(p.getActivo());
        r.setVigenciaDesde(p.getVigenciaDesde());
        return r;
    }

    public static List<PrecioVarianteResponseDTO> toResponseList(List<PrecioVariante> list) {
        if (list == null) return List.of();
        return list.stream().map(PrecioProductoMapper::toResponse).collect(Collectors.toList());
    }

    public static List<PrecioVarianteResumenDTO> toResumenList(List<PrecioVariante> list) {
        if (list == null) return List.of();
        return list.stream().map(PrecioProductoMapper::toResumen).collect(Collectors.toList());
    }

    public static PrecioVariante fromCrear(PrecioVarianteCrearDTO dto) {
        if (dto == null) return null;
        PrecioVariante p = new PrecioVariante();
        p.setMontoCentavos(dto.getMontoCentavos());
        p.setPrecioAnteriorCentavos(dto.getPrecioAnteriorCentavos());
        p.setPrecioSinIvaCentavos(dto.getPrecioSinIvaCentavos());
        p.setIvaPorcentaje(dto.getIvaPorcentaje());
        p.setMoneda(dto.getMoneda());
        p.setVigenciaDesde(dto.getVigenciaDesde());
        p.setVigenciaHasta(dto.getVigenciaHasta());
        p.setActivo(dto.getActivo() != null ? dto.getActivo() : Boolean.TRUE);
        p.setCreadoPor(null); // asignar si se recibe info del usuario
        // producto debe asignarse en el servicio usando dto.getProductoId()
        return p;
    }

    public static PrecioVariante fromCrearWithProducto(PrecioVarianteCrearDTO dto, Producto producto) {
        PrecioVariante p = fromCrear(dto);
        if (p != null) p.setProducto(producto);
        return p;
    }

    public static void applyActualizar(PrecioVarianteActualizarDTO dto, PrecioVariante entidad) {
        if (dto == null || entidad == null) return;
        if (dto.getMontoCentavos() != null) entidad.setMontoCentavos(dto.getMontoCentavos());
        if (dto.getPrecioAnteriorCentavos() != null) entidad.setPrecioAnteriorCentavos(dto.getPrecioAnteriorCentavos());
        if (dto.getPrecioSinIvaCentavos() != null) entidad.setPrecioSinIvaCentavos(dto.getPrecioSinIvaCentavos());
        if (dto.getIvaPorcentaje() != null) entidad.setIvaPorcentaje(dto.getIvaPorcentaje());
        if (dto.getMoneda() != null) entidad.setMoneda(dto.getMoneda());
        if (dto.getVigenciaDesde() != null) entidad.setVigenciaDesde(dto.getVigenciaDesde());
        if (dto.getVigenciaHasta() != null) entidad.setVigenciaHasta(dto.getVigenciaHasta());
        if (dto.getActivo() != null) entidad.setActivo(dto.getActivo());
        if (dto.getCreadoPor() != null) entidad.setCreadoPor(dto.getCreadoPor());
    }

}