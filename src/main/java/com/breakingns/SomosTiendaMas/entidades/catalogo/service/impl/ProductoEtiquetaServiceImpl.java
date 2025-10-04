package com.breakingns.SomosTiendaMas.entidades.catalogo.service.impl;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Etiqueta;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Producto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.ProductoEtiqueta;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.EtiquetaRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.ProductoEtiquetaRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.ProductoRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IProductoEtiquetaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class ProductoEtiquetaServiceImpl implements IProductoEtiquetaService {

    private final ProductoRepository productoRepo;
    private final EtiquetaRepository etiquetaRepo;
    private final ProductoEtiquetaRepository peRepo;

    public ProductoEtiquetaServiceImpl(ProductoRepository productoRepo,
                                       EtiquetaRepository etiquetaRepo,
                                       ProductoEtiquetaRepository peRepo) {
        this.productoRepo = productoRepo;
        this.etiquetaRepo = etiquetaRepo;
        this.peRepo = peRepo;
    }

    @Override
    public void asignarEtiqueta(Long productoId, Long etiquetaId) {
        Producto p = productoRepo.findById(productoId).orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + productoId));
        Etiqueta e = etiquetaRepo.findById(etiquetaId).orElseThrow(() -> new IllegalArgumentException("Etiqueta no encontrada: " + etiquetaId));

        // evitar duplicados activos
        Optional<ProductoEtiqueta> existing = peRepo.findByProductoIdAndEtiquetaIdAndDeletedAtIsNull(productoId, etiquetaId);
        if (existing.isPresent()) return;

        ProductoEtiqueta pe = new ProductoEtiqueta();
        pe.setProducto(p);
        pe.setEtiqueta(e);
        peRepo.save(pe);
    }

    @Override
    public void quitarEtiqueta(Long productoId, Long etiquetaId) {
        ProductoEtiqueta pe = peRepo.findByProductoIdAndEtiquetaIdAndDeletedAtIsNull(productoId, etiquetaId)
                .orElseThrow(() -> new IllegalArgumentException("Relacion etiqueta-producto no encontrada"));
        pe.setDeletedAt(LocalDateTime.now());
        peRepo.save(pe);
    }
}