package com.breakingns.SomosTiendaMas.entidades.catalogo.service;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.inventario.*;
import java.util.List;

public interface IInventarioProductoService {
    InventarioProductoDTO crear(InventarioProductoDTO dto);
    InventarioProductoDTO obtenerPorProductoId(Long productoId);
    DisponibilidadResponseDTO disponibilidad(Long productoId);
    ReservaStockResponseDTO reservarStock(ReservaStockRequestDTO request);
    OperacionSimpleResponseDTO liberarReserva(LiberacionReservaRequestDTO request);
    OperacionSimpleResponseDTO confirmarVenta(ConfirmacionVentaRequestDTO request);
    InventarioProductoDTO ajustarStock(Long productoId, long deltaOnHand, long deltaReserved);
    List<InventarioProductoDTO> listarTodos();
    List<InventarioProductoDTO> listarBajoStock(Long threshold);
    void eliminarPorProductoId(Long productoId);
}