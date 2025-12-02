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

    // helper para normalizar Integer/Long/primitive a long (seguro ante null)
    private static long safeNumber(Number n) {
        return n == null ? 0L : n.longValue();
    }

    @Override
    @Transactional(readOnly = true)
    public DisponibilidadResponseDTO disponibilidad(Long productoId) {
        InventarioProducto inv = repo.findByProductoId(productoId).orElse(null);
        long onHand = safeNumber(inv != null ? inv.getOnHand() : null);
        long reserved = safeNumber(inv != null ? inv.getReserved() : null);
        long disponible = Math.max(0L, onHand - reserved);
        return new DisponibilidadResponseDTO(productoId, disponible);
    }

    @Override
    public ReservaStockResponseDTO reservarStock(ReservaStockRequestDTO request) {
        if (request == null) throw new IllegalArgumentException("request null");
        InventarioProducto inv = repo.findByProductoId(request.getProductoId())
                .orElseThrow(() -> new EntityNotFoundException("Inventario no encontrado: " + request.getProductoId()));

        // si getOnHand()/getReserved() devuelven primitivos (long/int) no pueden ser null,
        // y si devuelven wrappers, safeNumber los normaliza.
        long onHand = safeNumber(inv != null ? inv.getOnHand() : null);
        long reserved = safeNumber(inv != null ? inv.getReserved() : null);
        long disponible = Math.max(0L, onHand - reserved);
        long qty = request.getCantidad();

        boolean ok = disponible >= qty && qty > 0;
        if (ok) {
            long newReserved = reserved + qty;
            // ajustar al tipo del campo en la entidad:
            // si reserved es Integer en la entidad: inv.setReserved((int)newReserved);
            // si reserved es Long/long en la entidad: inv.setReserved(newReserved);
            inv.setReserved((int) newReserved); // <-- adapta según el tipo real de tu entidad
            repo.save(inv);
            disponible = Math.max(0L, onHand - newReserved);
        }
        return new ReservaStockResponseDTO(request.getProductoId(), ok, disponible);
    }

    @Override
    public OperacionSimpleResponseDTO liberarReserva(LiberacionReservaRequestDTO request) {
        throw new UnsupportedOperationException("Liberación por orderRef no implementada en este servicio. Usa MovimientoInventarioService o adapta repo.");
    }

    @Override
    public OperacionSimpleResponseDTO confirmarVenta(ConfirmacionVentaRequestDTO request) {
        throw new UnsupportedOperationException("Confirmación de venta por orderRef no implementada en este servicio. Usa MovimientoInventarioService o adapta repo.");
    }

    @Override
    public InventarioProductoDTO ajustarStock(Long productoId, long deltaOnHand, long deltaReserved) {
        InventarioProducto inv = repo.findByProductoId(productoId)
                .orElseThrow(() -> new EntityNotFoundException("Inventario no encontrado: " + productoId));

        // para ajustarStock: usar safeNumber o asumir primitivo si la entidad ya lo usa
        long onHand = safeNumber(inv.getOnHand());
        long reserved = safeNumber(inv.getReserved());

        long newOnHand = Math.max(0L, onHand + deltaOnHand);
        long newReserved = Math.max(0L, reserved + deltaReserved);

        // adaptar cast según el tipo real en la entidad (Integer vs Long vs int)
        inv.setOnHand((int) newOnHand);
        inv.setReserved((int) newReserved);

        InventarioProducto saved = repo.save(inv);
        return InventarioMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventarioProductoDTO> listarTodos() {
        return repo.findAll().stream()
                .map(InventarioMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventarioProductoDTO> listarBajoStock(Long threshold) {
        int th = threshold != null ? threshold.intValue() : 0;
        return repo.findAll().stream()
                .filter(i -> (i.getOnHand() != null ? i.getOnHand() : 0) < th)
                .map(InventarioMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void eliminarPorProductoId(Long productoId) {
        repo.findByProductoId(productoId).ifPresent(inv -> {
            // si querés soft-delete y tu entidad tiene deletedAt, setearlo aquí.
            // Si no, borramos físicamente:
            repo.delete(inv);
        });
    }
}