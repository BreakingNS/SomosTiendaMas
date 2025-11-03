package com.breakingns.SomosTiendaMas.entidades.catalogo.service.impl;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.precio.*;
import com.breakingns.SomosTiendaMas.entidades.catalogo.mapper.PrecioProductoMapper;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.PrecioProducto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Producto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.PrecioProductoRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.ProductoRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IPrecioProductoService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PrecioProductoService implements IPrecioProductoService {

    private final PrecioProductoRepository repo;
    private final ProductoRepository productoRepo;

    public PrecioProductoService(PrecioProductoRepository repo, ProductoRepository productoRepo) {
        this.repo = repo;
        this.productoRepo = productoRepo;
    }

    @Override
    public PrecioProductoResponseDTO crear(PrecioProductoCrearDTO dto) {
        if (dto == null) throw new IllegalArgumentException("dto es null");
        if (dto.getProductoId() == null) throw new IllegalArgumentException("productoId es requerido");

        Producto producto = productoRepo.findById(dto.getProductoId())
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado: " + dto.getProductoId()));

        PrecioProducto entidad = PrecioProductoMapper.fromCrear(dto);
        entidad = PrecioProductoMapper.fromCrearWithProducto(dto, producto);

        // si el nuevo precio viene activo, desactivar otros activos del mismo producto
        if (Boolean.TRUE.equals(entidad.getActivo())) {
            List<PrecioProducto> activos = repo.findByProductoIdAndActivoTrueOrderByVigenciaDesdeDesc(dto.getProductoId());
            for (PrecioProducto p : activos) {
                p.setActivo(false);
            }
            if (!activos.isEmpty()) repo.saveAll(activos);
        }

        PrecioProducto saved = repo.save(entidad);
        return PrecioProductoMapper.toResponse(saved);
    }

    @Override
    public PrecioProductoResponseDTO actualizar(Long id, PrecioProductoActualizarDTO dto) {
        PrecioProducto existing = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Precio no encontrado: " + id));

        boolean activating = dto.getActivo() != null && dto.getActivo() && !Boolean.TRUE.equals(existing.getActivo());
        PrecioProductoMapper.applyActualizar(dto, existing);

        // si se activa este precio, desactivar los demás activos para el mismo producto
        if (activating && existing.getProducto() != null) {
            List<PrecioProducto> activos = repo.findByProductoIdAndActivoTrueOrderByVigenciaDesdeDesc(existing.getProducto().getId());
            for (PrecioProducto p : activos) {
                if (!p.getId().equals(existing.getId())) p.setActivo(false);
            }
            if (!activos.isEmpty()) repo.saveAll(activos);
        }

        PrecioProducto updated = repo.save(existing);
        return PrecioProductoMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public PrecioProductoResponseDTO obtenerPorId(Long id) {
        PrecioProducto p = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Precio no encontrado: " + id));
        if (p.getDeletedAt() != null) throw new EntityNotFoundException("Precio eliminado: " + id);
        return PrecioProductoMapper.toResponse(p);
    }

    @Override
    @Transactional(readOnly = true)
    public PrecioProductoResponseDTO obtenerVigentePorProductoId(Long productoId) {
        return repo.findFirstByProductoIdAndActivoTrueOrderByVigenciaDesdeDesc(productoId)
                .map(PrecioProductoMapper::toResponse)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrecioProductoResponseDTO> listarPorProductoId(Long productoId) {
        List<PrecioProducto> list = repo.findByProductoIdOrderByVigenciaDesdeDesc(productoId);
        return list.stream().map(PrecioProductoMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrecioProductoResponseDTO> buscarVigentesPorProductoIdEnFecha(Long productoId, LocalDateTime fecha) {
        if (fecha == null) fecha = LocalDateTime.now();
        List<PrecioProducto> list = repo.findByProductoIdAndVigenciaDesdeLessThanEqualAndVigenciaHastaGreaterThanEqual(productoId, fecha, fecha);
        return list.stream().map(PrecioProductoMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrecioProductoResponseDTO> listarActivas() {
        List<PrecioProducto> list = repo.findAllByDeletedAtIsNull();
        return list.stream().map(PrecioProductoMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public void eliminar(Long id) {
        PrecioProducto p = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Precio no encontrado: " + id));
        p.setDeletedAt(LocalDateTime.now());
        repo.save(p);
    }
}
