package com.breakingns.SomosTiendaMas.entidades.catalogo.service.impl;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.movimiento.*;
import com.breakingns.SomosTiendaMas.entidades.catalogo.mapper.MovimientoMapper;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.MovimientoInventario;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Producto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.MovimientoInventarioRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.ProductoRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IMovimientoInventarioService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class MovimientoInventarioService implements IMovimientoInventarioService {

    private final MovimientoInventarioRepository repo;
    private final ProductoRepository productoRepo;

    public MovimientoInventarioService(MovimientoInventarioRepository repo, ProductoRepository productoRepo) {
        this.repo = repo;
        this.productoRepo = productoRepo;
    }

    @Override
    public MovimientoResponseDTO crear(MovimientoCrearDTO dto) {
        if (dto == null) throw new IllegalArgumentException("dto es null");
        if (dto.getProductoId() == null) throw new IllegalArgumentException("productoId es requerido");

        Producto producto = productoRepo.findById(dto.getProductoId())
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado: " + dto.getProductoId()));

        MovimientoInventario entidad = MovimientoMapper.fromCrearWithProducto(dto, producto);
        MovimientoInventario saved = repo.save(entidad);
        return MovimientoMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public MovimientoResponseDTO obtenerPorId(Long id) {
        MovimientoInventario m = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Movimiento no encontrado: " + id));
        return MovimientoMapper.toResponse(m);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoResumenDTO> listarPorProductoId(Long productoId) {
        List<MovimientoInventario> list = repo.findByVarianteProductoId(productoId);
        return MovimientoMapper.toResumenList(list);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoResumenDTO> listarPorOrderRef(String orderRef) {
        if (orderRef == null) return List.of();
        List<MovimientoInventario> list = repo.findByOrderRef(orderRef);
        return MovimientoMapper.toResumenList(list);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoResumenDTO> filtrar(MovimientoFiltroDTO filtro) {
        if (filtro == null) return List.of();

        // Preferir consultas directas para los casos simples
        if (filtro.getProductoId() != null) {
            return listarPorProductoId(filtro.getProductoId());
        }
        if (filtro.getOrderRef() != null) {
            if (filtro.getTipo() != null) {
                List<MovimientoInventario> list = repo.findByOrderRefAndTipo(filtro.getOrderRef(), filtro.getTipo());
                return MovimientoMapper.toResumenList(list);
            }
            return listarPorOrderRef(filtro.getOrderRef());
        }

        // Caso general: cargar todos y aplicar filtros en memoria (simple, no optimizado)
        List<MovimientoInventario> all = repo.findAll();
        return all.stream()
                .filter(m -> filtro.getTipo() == null || filtro.getTipo().equals(m.getTipo()))
                .filter(m -> filtro.getFechaDesde() == null || !m.getCreatedAt().isBefore(filtro.getFechaDesde()))
                .filter(m -> filtro.getFechaHasta() == null || !m.getCreatedAt().isAfter(filtro.getFechaHasta()))
                .map(MovimientoMapper::toResumen)
                .collect(Collectors.toList());
    }
}