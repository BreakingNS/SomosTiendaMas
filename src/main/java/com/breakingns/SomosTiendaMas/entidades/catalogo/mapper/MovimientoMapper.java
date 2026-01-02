package com.breakingns.SomosTiendaMas.entidades.catalogo.mapper;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.movimiento.MovimientoCrearDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.movimiento.MovimientoResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.movimiento.MovimientoResumenDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.MovimientoInventario;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Producto;

import java.util.List;
import java.util.stream.Collectors;

public final class MovimientoMapper {

    private MovimientoMapper() {}

    public static MovimientoResponseDTO toResponse(MovimientoInventario m) {
        if (m == null) return null;
        return MovimientoResponseDTO.builder()
                .id(m.getId())
                .productoId(m.getProducto() != null ? m.getProducto().getId() : null)
                .tipo(m.getTipo())
                .cantidad(m.getCantidad())
                .orderRef(m.getOrderRef())
                .referenciaId(m.getReferenciaId())
                .metadataJson(m.getMetadataJson())
                .createdAt(m.getCreatedAt())
                .creadoPor(null) // rellenar en servicio si se conoce el usuario
                .saldoAntes(null) // calcular en servicio si se requiere
                .saldoDespues(null) // calcular en servicio si se requiere
                .build();
    }

    public static MovimientoResumenDTO toResumen(MovimientoInventario m) {
        if (m == null) return null;
        MovimientoResumenDTO r = new MovimientoResumenDTO();
        r.setId(m.getId());
        r.setProductoId(m.getProducto() != null ? m.getProducto().getId() : null);
        r.setTipo(m.getTipo());
        r.setCantidad(m.getCantidad());
        r.setOrderRef(m.getOrderRef());
        r.setCreatedAt(m.getCreatedAt());
        return r;
    }

    public static List<MovimientoResumenDTO> toResumenList(List<MovimientoInventario> movimientos) {
        if (movimientos == null) return List.of();
        return movimientos.stream().map(MovimientoMapper::toResumen).collect(Collectors.toList());
    }

    public static MovimientoInventario fromCrear(MovimientoCrearDTO dto) {
        if (dto == null) return null;
        MovimientoInventario m = new MovimientoInventario();
        // producto debe asignarse en servicio usando productoId
        m.setTipo(dto.getTipo());
        m.setCantidad(dto.getCantidad());
        m.setOrderRef(dto.getOrderRef());
        m.setReferenciaId(dto.getReferenciaId());
        m.setMetadataJson(dto.getMetadataJson());
        return m;
    }

    // utilidad: crear entidad y asignar producto por id (servicio debe resolver Producto)
    public static MovimientoInventario fromCrearWithProducto(MovimientoCrearDTO dto, Producto producto) {
        MovimientoInventario m = fromCrear(dto);
        if (m != null) m.setProducto(producto);
        return m;
    }
}