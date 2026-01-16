package com.breakingns.SomosTiendaMas.entidades.catalogo.service.impl;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_centralizado.PhysicalPropertiesDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Producto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.ProductoFisico;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.ProductoFisicoRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.ProductoRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IProductoFisicoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

// REVIEW: se va a utilizar solo VarianteFisicoServiceImpl y no ProductoFisicoServiceImpl, por lo que este controlador queda obsoleto
@Deprecated(since="2026-01-15", forRemoval=true)

@Service
public class ProductoFisicoServiceImpl implements IProductoFisicoService {
    
    private final ProductoFisicoRepository productoFisicoRepository;
    private final ProductoRepository productoRepository;

    public ProductoFisicoServiceImpl(ProductoFisicoRepository productoFisicoRepository,
                                     ProductoRepository productoRepository) {
        this.productoFisicoRepository = productoFisicoRepository;
        this.productoRepository = productoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PhysicalPropertiesDTO obtenerPorProductoId(Long productoId) {
        if (productoId == null) return null;
        Optional<ProductoFisico> opt = productoFisicoRepository.findByProducto_IdAndDeletedAtIsNull(productoId);
        return opt.map(this::toDto).orElse(null);
    }

    @Override
    @Transactional
    public PhysicalPropertiesDTO crearOActualizarPorProducto(Long productoId, PhysicalPropertiesDTO dto) {
        if (productoId == null || dto == null) return null;

        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + productoId));

        ProductoFisico entidad = productoFisicoRepository.findByProducto_Id(productoId)
                .orElseGet(() -> {
                    ProductoFisico p = new ProductoFisico();
                    p.setProducto(producto);
                    return p;
                });

        // Mapear campos del DTO a la entidad
        entidad.setWeightGrams(dto.getWeightGrams());
        entidad.setWidthMm(dto.getWidthMm());
        entidad.setHeightMm(dto.getHeightMm());
        entidad.setDepthMm(dto.getDepthMm());
        entidad.setPackageWeightGrams(dto.getPackageWeightGrams());
        entidad.setPackageWidthMm(dto.getPackageWidthMm());
        entidad.setPackageHeightMm(dto.getPackageHeightMm());
        entidad.setPackageDepthMm(dto.getPackageDepthMm());

        ProductoFisico saved = productoFisicoRepository.save(entidad);
        return toDto(saved);
    }

    @Override
    @Transactional
    public void eliminarPorProductoId(Long productoId) {
        if (productoId == null) return;
        productoFisicoRepository.findByProducto_Id(productoId).ifPresent(entity -> {
            productoFisicoRepository.delete(entity);
        });
    }

    // Mappers
    private PhysicalPropertiesDTO toDto(ProductoFisico e) {
        if (e == null) return null;
        PhysicalPropertiesDTO dto = new PhysicalPropertiesDTO();
        dto.setWeightGrams(e.getWeightGrams());
        dto.setWidthMm(e.getWidthMm());
        dto.setHeightMm(e.getHeightMm());
        dto.setDepthMm(e.getDepthMm());
        dto.setPackageWeightGrams(e.getPackageWeightGrams());
        dto.setPackageWidthMm(e.getPackageWidthMm());
        dto.setPackageHeightMm(e.getPackageHeightMm());
        dto.setPackageDepthMm(e.getPackageDepthMm());
        return dto;
    }
}