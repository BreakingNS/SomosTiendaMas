package com.breakingns.SomosTiendaMas.entidades.catalogo.service.impl;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.OpcionProducto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Producto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.ValorOpcionProducto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.OpcionProductoRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.ProductoRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.ValorOpcionProductoRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IOpcionProductoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class OpcionProductoServiceImpl implements IOpcionProductoService {

    private final ProductoRepository productoRepository;
    private final OpcionProductoRepository opcionRepository;
    private final ValorOpcionProductoRepository valorRepository;

    public OpcionProductoServiceImpl(ProductoRepository productoRepository,
                                     OpcionProductoRepository opcionRepository,
                                     ValorOpcionProductoRepository valorRepository) {
        this.productoRepository = productoRepository;
        this.opcionRepository = opcionRepository;
        this.valorRepository = valorRepository;
    }

    @Override
    public OpcionProducto crearOpcion(Long productoId, String nombre, Integer orden) {
        Producto p = productoRepository.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + productoId));
        OpcionProducto o = new OpcionProducto();
        o.setProducto(p);
        o.setNombre(nombre);
        o.setOrden(orden != null ? orden : 0);
        return opcionRepository.save(o);
    }

    @Override
    public OpcionProducto actualizarOpcion(Long opcionId, String nombre, Integer orden) {
        OpcionProducto o = opcionRepository.findById(opcionId)
                .orElseThrow(() -> new IllegalArgumentException("Opción no encontrada: " + opcionId));
        if (nombre != null) o.setNombre(nombre);
        if (orden != null) o.setOrden(orden);
        return opcionRepository.save(o);
    }

    @Override
    public void eliminarOpcion(Long opcionId) {
        OpcionProducto o = opcionRepository.findById(opcionId)
                .orElseThrow(() -> new IllegalArgumentException("Opción no encontrada: " + opcionId));
        opcionRepository.delete(o);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OpcionProducto> listarOpciones(Long productoId) {
        Producto p = productoRepository.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + productoId));
        return opcionRepository.findByProductoOrderByOrdenAsc(p);
    }

    @Override
    public ValorOpcionProducto crearValor(Long opcionId, String valor, String slug, Integer orden) {
        OpcionProducto o = opcionRepository.findById(opcionId)
                .orElseThrow(() -> new IllegalArgumentException("Opción no encontrada: " + opcionId));
        ValorOpcionProducto v = new ValorOpcionProducto();
        v.setOpcion(o);
        v.setValor(valor);
        v.setSlug(slug);
        v.setOrden(orden != null ? orden : 0);
        return valorRepository.save(v);
    }

    @Override
    public void eliminarValor(Long valorId) {
        ValorOpcionProducto v = valorRepository.findById(valorId)
                .orElseThrow(() -> new IllegalArgumentException("Valor no encontrado: " + valorId));
        valorRepository.delete(v);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ValorOpcionProducto> listarValores(Long opcionId) {
        OpcionProducto o = opcionRepository.findById(opcionId)
                .orElseThrow(() -> new IllegalArgumentException("Opción no encontrada: " + opcionId));
        return valorRepository.findByOpcionOrderByOrdenAsc(o);
    }
}
