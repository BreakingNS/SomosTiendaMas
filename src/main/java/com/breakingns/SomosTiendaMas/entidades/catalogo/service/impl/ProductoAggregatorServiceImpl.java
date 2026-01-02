package com.breakingns.SomosTiendaMas.entidades.catalogo.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto.ProductoResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_centralizado.ProductoDetalleResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.imagen.ImagenProductoDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.precio.PrecioProductoResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.inventario.DisponibilidadResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.opcion.OpcionResumenDTO;

import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IProductoService;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IImagenProductoService;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IPrecioProductoService;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IInventarioProductoService;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IOpcionService;

import java.util.List;
import java.util.ArrayList;

@Service
public class ProductoAggregatorServiceImpl {

    private final IProductoService productoService;
    private final IImagenProductoService imagenService;
    private final IPrecioProductoService precioService;
    private final IInventarioProductoService inventarioProductoService;
    private final IOpcionService opcionService;

    public ProductoAggregatorServiceImpl(
            IProductoService productoService,
            IImagenProductoService imagenService,
            IPrecioProductoService precioService,
            IInventarioProductoService inventarioProductoService,
            IOpcionService opcionService
    ) {
        this.productoService = productoService;
        this.imagenService = imagenService;
        this.precioService = precioService;
        this.inventarioProductoService = inventarioProductoService;
        this.opcionService = opcionService;
    }

    @Transactional(readOnly = true)
    public List<ProductoDetalleResponseDTO> buildDetallesAll() {
        List<ProductoResponseDTO> productos = productoService.listarActivas();
        if (productos == null || productos.isEmpty()) return new ArrayList<>();

        List<ProductoDetalleResponseDTO> out = new ArrayList<>(productos.size());
        for (ProductoResponseDTO p : productos) {
            if (p == null || p.getId() == null) continue;
            ProductoDetalleResponseDTO detalle = buildDetalleById(p.getId());
            if (detalle != null) out.add(detalle);
        }
        return out;
    }

    @Transactional(readOnly = true)
    public ProductoDetalleResponseDTO buildDetalleById(Long productoId) {
        if (productoId == null) return null;

        // Producto
        ProductoResponseDTO producto = productoService.obtenerPorId(productoId);
        if (producto == null) return null;

        // Imágenes
        List<ImagenProductoDTO> imagenes = imagenService.listarPorProductoId(productoId);

        // Precio vigente
        PrecioProductoResponseDTO precio = precioService.obtenerVigentePorProductoId(productoId);

        // Stock / disponibilidad
        DisponibilidadResponseDTO stock = inventarioProductoService.disponibilidad(productoId);

        // Opciones del producto (resumen)
        List<OpcionResumenDTO> opciones = opcionService.listarOpcionesPorProductoId(productoId);

        // Armar DTO agregado
        ProductoDetalleResponseDTO dto = new ProductoDetalleResponseDTO();
        dto.setProducto(producto);
        dto.setImagenes(imagenes);
        dto.setPrecio(precio);
        dto.setStock(stock);
        dto.setOpciones(opciones);

        return dto;
    }
}