package com.breakingns.SomosTiendaMas.entidades.catalogo.service.impl;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.etiqueta.EtiquetaResumenDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.mapper.EtiquetaMapper;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Etiqueta;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Producto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.ProductoEtiqueta;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.EtiquetaRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.ProductoEtiquetaRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.ProductoRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IProductoEtiquetaService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductoEtiquetaService implements IProductoEtiquetaService {

    private final ProductoEtiquetaRepository repo;
    private final ProductoRepository productoRepo;
    private final EtiquetaRepository etiquetaRepo;

    public ProductoEtiquetaService(ProductoEtiquetaRepository repo,
                                   ProductoRepository productoRepo,
                                   EtiquetaRepository etiquetaRepo) {
        this.repo = repo;
        this.productoRepo = productoRepo;
        this.etiquetaRepo = etiquetaRepo;
    }

    @Override
    public EtiquetaResumenDTO asignarEtiqueta(Long productoId, Long etiquetaId) {
        if (productoId == null || etiquetaId == null) throw new IllegalArgumentException("productoId y etiquetaId requeridos");

        // verificar existencia producto y etiqueta
        Producto producto = productoRepo.findById(productoId)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado: " + productoId));
        Etiqueta etiqueta = etiquetaRepo.findById(etiquetaId)
                .orElseThrow(() -> new EntityNotFoundException("Etiqueta no encontrada: " + etiquetaId));

        // si ya existe relación activa, devolver resumen
        repo.findByProductoIdAndEtiquetaIdAndDeletedAtIsNull(productoId, etiquetaId).ifPresent(rel -> {
            throw new IllegalStateException("Etiqueta ya asignada al producto");
        });

        ProductoEtiqueta rel = new ProductoEtiqueta();
        rel.setProducto(producto);
        rel.setEtiqueta(etiqueta);
        // auditoría soft-create gestionada por JPA/Auditor; si no, puede setearse createdAt
        ProductoEtiqueta saved = repo.save(rel);
        return EtiquetaMapper.toResumen(saved.getEtiqueta());
    }

    @Override
    public void quitarEtiqueta(Long productoId, Long etiquetaId) {
        if (productoId == null || etiquetaId == null) throw new IllegalArgumentException("productoId y etiquetaId requeridos");
        ProductoEtiqueta rel = repo.findByProductoIdAndEtiquetaIdAndDeletedAtIsNull(productoId, etiquetaId)
                .orElseThrow(() -> new EntityNotFoundException("Relación producto-etiqueta no encontrada"));
        rel.setDeletedAt(LocalDateTime.now());
        repo.save(rel);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EtiquetaResumenDTO> listarEtiquetasPorProductoId(Long productoId) {
        List<ProductoEtiqueta> list = repo.findByProductoId(productoId);
        return list.stream()
                .filter(r -> r.getDeletedAt() == null)
                .map(ProductoEtiqueta::getEtiqueta)
                .filter(e -> e != null)
                .map(EtiquetaMapper::toResumen)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> listarProductoIdsPorEtiquetaId(Long etiquetaId) {
        List<ProductoEtiqueta> list = repo.findByEtiquetaId(etiquetaId);
        return list.stream()
                .filter(r -> r.getDeletedAt() == null)
                .map(ProductoEtiqueta::getProducto)
                .filter(p -> p != null)
                .map(Producto::getId)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeRelacion(Long productoId, Long etiquetaId) {
        return repo.findByProductoIdAndEtiquetaIdAndDeletedAtIsNull(productoId, etiquetaId).isPresent();
    }

    @Override
    public void eliminarRelacion(Long id) {
        ProductoEtiqueta rel = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Relación no encontrada: " + id));
        rel.setDeletedAt(LocalDateTime.now());
        repo.save(rel);
    }
}
