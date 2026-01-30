package com.breakingns.SomosTiendaMas.entidades.catalogo.service.impl;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.movimiento.*;
import com.breakingns.SomosTiendaMas.entidades.catalogo.mapper.MovimientoMapper;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.MovimientoInventario;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Producto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Variante;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.MovimientoInventarioRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.ProductoRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IMovimientoInventarioService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class MovimientoInventarioService implements IMovimientoInventarioService {

    private final MovimientoInventarioRepository repo;
    private final ProductoRepository productoRepo;
    private final com.breakingns.SomosTiendaMas.entidades.catalogo.repository.VarianteRepository varianteRepo;
    private final ApplicationContext applicationContext;

    public MovimientoInventarioService(MovimientoInventarioRepository repo, ProductoRepository productoRepo,
                                       com.breakingns.SomosTiendaMas.entidades.catalogo.repository.VarianteRepository varianteRepo,
                                       ApplicationContext applicationContext) {
        this.repo = repo;
        this.productoRepo = productoRepo;
        this.varianteRepo = varianteRepo;
        this.applicationContext = applicationContext;
    }

    @Override
    public MovimientoResponseDTO crear(MovimientoCrearDTO dto) {
        if (dto == null) throw new IllegalArgumentException("dto es null");
        if (dto.getProductoId() == null) throw new IllegalArgumentException("productoId es requerido");

        Producto producto = productoRepo.findById(dto.getProductoId())
            .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado: " + dto.getProductoId()));

        // Construir entidad sin variante inicialmente
        MovimientoInventario entidad = MovimientoMapper.fromCrear(dto);

        // Si se indicó varianteId, obtener una referencia gestionada y asignarla PRIMERO
        if (dto.getVarianteId() != null) {
            Variante variante = varianteRepo.findById(dto.getVarianteId())
                .orElseThrow(() -> new EntityNotFoundException("Variante no encontrada: " + dto.getVarianteId()));
            entidad.setVariante(variante);
        }

        // Asignar producto solo si ya tenemos la variante (evita crear Variante transitoria)
        if (entidad.getVariante() != null) {
            entidad.setProducto(producto);
        }

        // Idempotencia: si viene orderRef + tipo (+ varianteId), devolver movimiento existente
        if (dto.getOrderRef() != null && dto.getTipo() != null) {
            MovimientoInventario existente = null;
            if (dto.getVarianteId() != null) {
                existente = repo.findFirstByOrderRefAndTipoAndVarianteId(dto.getOrderRef(), dto.getTipo(), dto.getVarianteId())
                        .orElse(null);
            } else {
                existente = repo.findFirstByOrderRefAndTipoOrderByCreatedAtAsc(dto.getOrderRef(), dto.getTipo())
                        .orElse(null);
            }
            if (existente != null) {
                return MovimientoMapper.toResponse(existente);
            }
        }

        try {
            MovimientoInventario saved = repo.save(entidad);
            return MovimientoMapper.toResponse(saved);
        } catch (DataIntegrityViolationException ex) {
            // Posible insert concurrente por otro hilo -> intentar recuperar el movimiento ya existente
            MovimientoInventario existente = null;
            try {
                MovimientoInventarioService proxy = applicationContext.getBean(MovimientoInventarioService.class);
                existente = proxy.fetchExistingMovimiento(dto.getOrderRef(), dto.getTipo(), dto.getVarianteId());
            } catch (Exception e) {
                // ignore secondary errors; rethrow original
            }
            if (existente != null) {
                return MovimientoMapper.toResponse(existente);
            }
            throw ex;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public MovimientoInventario fetchExistingMovimiento(String orderRef, com.breakingns.SomosTiendaMas.entidades.catalogo.enums.TipoMovimientoInventario tipo, Long varianteId) {
        if (orderRef == null || tipo == null) return null;
        if (varianteId != null) {
            return repo.findFirstByOrderRefAndTipoAndVarianteId(orderRef, tipo, varianteId).orElse(null);
        }
        return repo.findFirstByOrderRefAndTipoOrderByCreatedAtAsc(orderRef, tipo).orElse(null);
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
    public List<MovimientoResumenDTO> listarPorVarianteId(Long varianteId) {
        List<MovimientoInventario> list = repo.findByVarianteId(varianteId);
        return MovimientoMapper.toResumenList(list);
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