package com.breakingns.SomosTiendaMas.entidades.catalogo.service.impl;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.InventarioVariante;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Producto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.VarianteOpcionValor;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.ValorOpcionProducto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.VarianteProducto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.InventarioVarianteRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.ProductoRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.VarianteOpcionValorRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.VarianteProductoRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.ValorOpcionProductoRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IVarianteProductoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class VarianteProductoServiceImpl implements IVarianteProductoService {

    private final VarianteProductoRepository varianteRepository;
    private final ProductoRepository productoRepository;
    private final InventarioVarianteRepository inventarioRepository;
    private final ValorOpcionProductoRepository valorRepository;
    private final VarianteOpcionValorRepository vovRepository;

    public VarianteProductoServiceImpl(VarianteProductoRepository varianteRepository,
                                       ProductoRepository productoRepository,
                                       InventarioVarianteRepository inventarioRepository,
                                       ValorOpcionProductoRepository valorRepository,
                                       VarianteOpcionValorRepository vovRepository) {
        this.varianteRepository = varianteRepository;
        this.productoRepository = productoRepository;
        this.inventarioRepository = inventarioRepository;
        this.valorRepository = valorRepository;
        this.vovRepository = vovRepository;
    }

    @Override
    public VarianteProducto crear(Long productoId, VarianteProducto variante) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + productoId));
        variante.setProducto(producto);
        VarianteProducto saved = varianteRepository.save(variante);

        inventarioRepository.findByVariante(saved).orElseGet(() -> {
            InventarioVariante inv = new InventarioVariante();
            inv.setVariante(saved);
            inv.setOnHand(0L);
            inv.setReserved(0L);
            return inventarioRepository.save(inv);
        });
        return saved;
    }

    @Override
    public VarianteProducto actualizar(Long id, VarianteProducto cambios) {
        VarianteProducto v = varianteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Variante no encontrada: " + id));
        if (cambios.getSku() != null) v.setSku(cambios.getSku());
        if (cambios.getCodigoBarras() != null) v.setCodigoBarras(cambios.getCodigoBarras());
        if (cambios.getPesoGramos() != null) v.setPesoGramos(cambios.getPesoGramos());
        if (cambios.getAltoMm() != null) v.setAltoMm(cambios.getAltoMm());
        if (cambios.getAnchoMm() != null) v.setAnchoMm(cambios.getAnchoMm());
        if (cambios.getLargoMm() != null) v.setLargoMm(cambios.getLargoMm());
        if (cambios.getMetadataJson() != null) v.setMetadataJson(cambios.getMetadataJson());
        return varianteRepository.save(v);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<VarianteProducto> obtener(Long id) {
        return varianteRepository.findById(id).filter(v -> v.getDeletedAt() == null);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<VarianteProducto> obtenerPorSku(String sku) {
        return varianteRepository.findBySku(sku).filter(v -> v.getDeletedAt() == null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VarianteProducto> listarPorProducto(Long productoId) {
        Producto p = productoRepository.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + productoId));
        return varianteRepository.findByProducto(p).stream().filter(v -> v.getDeletedAt() == null).toList();
    }

    @Override
    public void eliminarLogico(Long id, String usuario) {
        VarianteProducto v = varianteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Variante no encontrada: " + id));
        v.setDeletedAt(LocalDateTime.now());
        v.setUpdatedBy(usuario);
        varianteRepository.save(v);
    }

    @Override
    public void asignarValores(Long varianteId, List<Long> valorIds) {
        VarianteProducto variante = varianteRepository.findById(varianteId)
                .orElseThrow(() -> new IllegalArgumentException("Variante no encontrada: " + varianteId));
        Producto producto = variante.getProducto();

        List<ValorOpcionProducto> valores = valorRepository.findAllById(valorIds);
        if (valores.size() != valorIds.size()) {
            throw new IllegalArgumentException("Algunos valores no existen");
        }
        // Validar que cada valor pertenezca a una opción del mismo producto
        boolean invalidos = valores.stream().anyMatch(v -> v.getOpcion() == null
                || v.getOpcion().getProducto() == null
                || !v.getOpcion().getProducto().getId().equals(producto.getId()));
        if (invalidos) {
            throw new IllegalArgumentException("Valores no pertenecen al producto de la variante");
        }

        // Reemplazar asignaciones
        vovRepository.deleteByVariante(variante);
        for (ValorOpcionProducto valor : valores) {
            VarianteOpcionValor vov = new VarianteOpcionValor();
            vov.setVariante(variante);
            vov.setValor(valor);
            vovRepository.save(vov);
        }
    }
}
