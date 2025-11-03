package com.breakingns.SomosTiendaMas.entidades.catalogo.service;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.movimiento.*;
import java.util.List;

public interface IMovimientoInventarioService {
    MovimientoResponseDTO crear(MovimientoCrearDTO dto);
    MovimientoResponseDTO obtenerPorId(Long id);
    List<MovimientoResumenDTO> listarPorProductoId(Long productoId);
    List<MovimientoResumenDTO> listarPorOrderRef(String orderRef);
    List<MovimientoResumenDTO> filtrar(MovimientoFiltroDTO filtro);
}