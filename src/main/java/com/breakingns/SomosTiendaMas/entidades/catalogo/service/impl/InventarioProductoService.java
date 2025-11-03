package com.breakingns.SomosTiendaMas.entidades.catalogo.service.impl;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.inventario.*;
import com.breakingns.SomosTiendaMas.entidades.catalogo.mapper.InventarioMapper;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.InventarioProducto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Producto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.InventarioProductoRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.ProductoRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IInventarioProductoService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class InventarioProductoService implements IInventarioProductoService {

    private final InventarioProductoRepository repo;
    private final ProductoRepository productoRepo;

    public InventarioProductoService(InventarioProductoRepository repo, ProductoRepository productoRepo) {
        this.repo = repo;
        this.productoRepo = productoRepo;
    }

    @Override
    public InventarioProductoDTO crear(InventarioProductoDTO dto) {
        if (dto == null) throw new IllegalArgumentException("dto null");
        if (dto.getProductoId() == null) throw new IllegalArgumentException("productoId es requerido");

        Producto producto = productoRepo.findById(dto.getProductoId())
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado: " + dto.getProductoId()));

        InventarioProducto entidad = InventarioMapper.fromDto(dto);
        entidad.setProducto(producto);
        InventarioProducto saved = repo.save(entidad);
        return InventarioMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public InventarioProductoDTO obtenerPorProductoId(Long productoId) {
        InventarioProducto inv = repo.findByProductoId(productoId)
                .orElseThrow(() -> new EntityNotFoundException("Inventario no encontrado para producto: " + productoId));
        return InventarioMapper.toDto(inv);
    }

    @Override
    @Transactional(readOnly = true)
    public DisponibilidadResponseDTO disponibilidad(Long productoId) {
        InventarioProducto inv = repo.findByProductoId(productoId).orElse(null);
        long disponible = inv == null ? 0L : Math.max(0L, inv.getOnHand() - inv.getReserved());
        return new DisponibilidadResponseDTO(productoId, disponible);
    }

    @Override
    public ReservaStockResponseDTO reservarStock(ReservaStockRequestDTO request) {
        if (request == null) throw new IllegalArgumentException("request null");
        InventarioProducto inv = repo.findByProductoId(request.getProductoId())
                .orElseThrow(() -> new EntityNotFoundException("Inventario no encontrado: " + request.getProductoId()));

        long disponible = Math.max(0L, inv.getOnHand() - inv.getReserved());
        boolean ok = disponible >= request.getCantidad();
        if (ok) {
            inv.setReserved(inv.getReserved() + request.getCantidad());
            repo.save(inv);
            disponible = Math.max(0L, inv.getOnHand() - inv.getReserved());
        }
        return new ReservaStockResponseDTO(request.getProductoId(), ok, disponible);
    }

    @Override
    public OperacionSimpleResponseDTO liberarReserva(LiberacionReservaRequestDTO request) {
        if (request == null) throw new IllegalArgumentException("request null");
        throw new UnsupportedOperationException("Liberación por orderRef no implementada en este servicio. Usa MovimientoInventarioService o adapta repo.");
    }

    @Override
    public OperacionSimpleResponseDTO confirmarVenta(ConfirmacionVentaRequestDTO request) {
        if (request == null) throw new IllegalArgumentException("request null");
        throw new UnsupportedOperationException("Confirmación de venta por orderRef no implementada en este servicio. Usa MovimientoInventarioService o adapta repo.");
    }

    @Override
    public InventarioProductoDTO ajustarStock(Long productoId, long deltaOnHand, long deltaReserved) {
        InventarioProducto inv = repo.findByProductoId(productoId)
                .orElseThrow(() -> new EntityNotFoundException("Inventario no encontrado: " + productoId));
        inv.setOnHand(Math.max(0L, inv.getOnHand() + deltaOnHand));
        inv.setReserved(Math.max(0L, inv.getReserved() + deltaReserved));
        InventarioProducto saved = repo.save(inv);
        return InventarioMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventarioProductoDTO> listarTodos() {
        return repo.findAll().stream()
                .filter(i -> i.getDeletedAt() == null)
                .map(InventarioMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventarioProductoDTO> listarBajoStock(Long threshold) {
        return repo.findAll().stream()
                .filter(i -> i.getDeletedAt() == null && i.getOnHand() < threshold)
                .map(InventarioMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void eliminarPorProductoId(Long productoId) {
        repo.findByProductoId(productoId).ifPresent(inv -> {
            inv.setDeletedAt(LocalDateTime.now());
            repo.save(inv);
        });
    }
}