package com.breakingns.SomosTiendaMas.entidades.catalogo.service;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.inventario.*;
import java.util.List;

public interface IInventarioVarianteService {
    InventarioVarianteDTO crear(InventarioVarianteDTO dto);
    InventarioVarianteDTO obtenerPorVarianteId(Long VarianteId);
    DisponibilidadResponseDTO disponibilidad(Long VarianteId);
    ReservaStockResponseDTO reservarStock(ReservaStockRequestDTO request);
    OperacionSimpleResponseDTO liberarReserva(LiberacionReservaRequestDTO request);
    OperacionSimpleResponseDTO confirmarVenta(ConfirmacionVentaRequestDTO request);
    InventarioVarianteDTO ajustarStock(Long VarianteId, long deltaOnHand, long deltaReserved);
    List<InventarioVarianteDTO> listarTodos();
    List<InventarioVarianteDTO> listarBajoStock(Long threshold);
    void eliminarPorVarianteId(Long VarianteId);
}