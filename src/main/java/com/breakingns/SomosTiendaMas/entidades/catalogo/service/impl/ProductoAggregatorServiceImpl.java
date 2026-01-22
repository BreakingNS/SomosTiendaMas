// NOTE: deprecado, ahora se utiliza ProductoCentralizadoServiceImpl.
/* 
package com.breakingns.SomosTiendaMas.entidades.catalogo.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto.ProductoResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.imagen.ImagenVarianteDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.precio.PrecioVarianteResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.inventario.DisponibilidadResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.opcion.OpcionResumenDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IProductoService;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IImagenVarianteService;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IPrecioVarianteService;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IInventarioVarianteService;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IOpcionService;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IVarianteOpcionService;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IVarianteService;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.precio.PrecioVarianteResumenDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IVarianteFisicoService;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_centralizado.PhysicalPropertiesDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_centralizado.ProductoCentralizadoResponseDTO;

import java.util.List;
import java.util.ArrayList;

@Service
public class ProductoAggregatorServiceImpl {

    private final IProductoService productoService;
    private final IImagenVarianteService imagenService;
    private final IInventarioVarianteService inventarioVarianteService;
    private final IOpcionService opcionService;
    private final IVarianteService varianteService;
    private final IPrecioVarianteService precioVarianteService;
    private final IVarianteOpcionService varianteOpcionService;
    private final IImagenVarianteService imagenVarianteService;
    private final IVarianteFisicoService varianteFisicoService;

    public ProductoAggregatorServiceImpl(
            IProductoService productoService,
            IImagenVarianteService imagenService,
                IInventarioVarianteService inventarioVarianteService,
                IOpcionService opcionService,
                IVarianteService varianteService,
                IPrecioVarianteService precioVarianteService,
                IVarianteOpcionService varianteOpcionService,
                IImagenVarianteService imagenVarianteService,
                IVarianteFisicoService varianteFisicoService
    ) {
        this.productoService = productoService;
        this.imagenService = imagenService;
        this.inventarioVarianteService = inventarioVarianteService;
        this.opcionService = opcionService;
        this.varianteService = varianteService;
        this.precioVarianteService = precioVarianteService;
        this.varianteOpcionService = varianteOpcionService;
        this.imagenVarianteService = imagenVarianteService;
        this.varianteFisicoService = varianteFisicoService;
    }

    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> buildDetallesAll() {
        List<ProductoCentralizadoResponseDTO> productos = productoService.listarActivas();
        if (productos == null || productos.isEmpty()) return new ArrayList<>();

        List<ProductoResponseDTO> out = new ArrayList<>(productos.size());
        for (ProductoCentralizadoResponseDTO p : productos) {
            if (p == null || p.getId() == null) continue;
            ProductoResponseDTO detalle = buildDetalleById(p.getId());
            if (detalle != null) out.add(detalle);
        }

        return out;
    }

    @Transactional(readOnly = true)
    public ProductoResponseDTO buildDetalleById(Long productoId) {
        if (productoId == null) return null;

        // Producto
        ProductoCentralizadoResponseDTO producto = productoService.obtenerPorId(productoId);
        if (producto == null) return null;
        /* 
        // Imágenes (migradas a variantes: listar imágenes de variante/producto)
        List<ImagenVarianteDTO> imagenes = imagenService.listarPorVarianteId(productoId);

        // Obtener variante default y usar sus datos (precio/stock) como fuente de verdad
        var varianteDefault = varianteService.obtenerDefaultByProductoId(productoId);

        PrecioVarianteResponseDTO precio = null;
        DisponibilidadResponseDTO stock = null;
        if (varianteDefault != null) {
            PrecioVarianteResponseDTO dtoPrecio = precioVarianteService.obtenerVigentePorVarianteId(varianteDefault.getId());
            if (dtoPrecio != null) {
                precio = dtoPrecio;
            }
            // intentar usar stock resuelto desde variante DTO si está poblado
            if (varianteDefault.getStockResuelto() != null) {
                DisponibilidadResponseDTO s = new DisponibilidadResponseDTO();
                s.setVarianteId(varianteDefault.getId());
                s.setDisponible(varianteDefault.getStockResuelto());
                stock = s;
            } else {
                // fallback: consulta inventario por producto
                stock = inventarioVarianteService.disponibilidad(varianteDefault.getId());
            }
        } else {
            // si no hay variante default, intentar obtener algún precio activo por producto
            List<PrecioVarianteResponseDTO> activos = precioVarianteService.listarActivas();
            if (activos != null) {
                precio = activos.stream().filter(p -> productoId.equals(p.getProductoId())).findFirst().orElse(null);
            }
            stock = inventarioVarianteService.disponibilidad(productoId);
        }

        // Opciones del producto (resumen)
        List<OpcionResumenDTO> opciones = opcionService.listarOpcionesPorProductoId(productoId);*/
        
        // Armar DTO agregado
        /*
        ProductoResponseDTO dto = new ProductoResponseDTO();
        dto.setProducto(producto);
        */
        // REVIEW: se deja de utilizar fisicar para producto, solo para variantes.
        // @Deprecated(since="2026-01-15", forRemoval=true)
        /*
        dto.setImagenes(imagenes);
        dto.setPrecio(precio);
        dto.setStock(stock);
        
        dto.setOpciones(opciones);
        */
        /*
        // Poblar precio por variante en la lista de variantes del producto (si está presente)
        try {
            if (producto.getVariantes() != null && !producto.getVariantes().isEmpty()) {
                for (var v : producto.getVariantes()) {
                    try {
                        if (v != null && v.getId() != null) {
                            var p = precioVarianteService.obtenerVigentePorVarianteId(v.getId());
                            if (p != null) {
                                // poblar campo legacy y el objeto resumen compacto
                                v.setPrecioCentavos(p.getMontoCentavos());
                                PrecioVarianteResumenDTO r = new PrecioVarianteResumenDTO();
                                r.setId(p.getId());
                                r.setProductoId(p.getProductoId());
                                r.setMontoCentavos(p.getMontoCentavos());
                                r.setPrecioAnteriorCentavos(p.getPrecioAnteriorCentavos());
                                r.setPrecioSinIvaCentavos(p.getPrecioSinIvaCentavos());
                                r.setIvaPorcentaje(p.getIvaPorcentaje());
                                r.setDescuentoPorcentaje(p.getDescuentoPorcentaje());
                                r.setMoneda(p.getMoneda());
                                r.setActivo(p.getActivo());
                                r.setVigenciaDesde(p.getVigenciaDesde());
                                r.setVigenciaHasta(p.getVigenciaHasta());
                                v.setPrecio(r);
                            } else {
                                // fallback: intentar obtener cualquier precio histórico para esta variante
                                try {
                                    var list = precioVarianteService.listarPorVarianteId(v.getId());
                                    if (list != null && !list.isEmpty()) {
                                        var p2 = list.get(0);
                                        v.setPrecioCentavos(p2.getMontoCentavos());
                                        PrecioVarianteResumenDTO r2 = new PrecioVarianteResumenDTO();
                                        r2.setId(p2.getId());
                                        r2.setProductoId(p2.getProductoId());
                                        r2.setMontoCentavos(p2.getMontoCentavos());
                                        r2.setPrecioAnteriorCentavos(p2.getPrecioAnteriorCentavos());
                                        r2.setPrecioSinIvaCentavos(p2.getPrecioSinIvaCentavos());
                                        r2.setIvaPorcentaje(p2.getIvaPorcentaje());
                                        r2.setDescuentoPorcentaje(p2.getDescuentoPorcentaje());
                                        r2.setMoneda(p2.getMoneda());
                                        r2.setActivo(p2.getActivo());
                                        r2.setVigenciaDesde(p2.getVigenciaDesde());
                                        r2.setVigenciaHasta(p2.getVigenciaHasta());
                                        v.setPrecio(r2);
                                    }
                                } catch (Exception ignore) {}
                            }
                            // poblar disponibilidad por variante
                            try {
                                DisponibilidadResponseDTO d = inventarioVarianteService.disponibilidad(v.getId());
                                if (d != null) v.setDisponible(d);
                            } catch (Exception e) {
                                // noop
                            }
                            try {
                                var opc = varianteOpcionService.obtenerVarianteConOpcionesConValores(v.getId());
                                if (opc != null) v.setOpciones(opc.getOpciones());
                            } catch (Exception ex) {
                                // noop
                            }
                            try {
                                var imgs = imagenVarianteService.listarPorVarianteId(v.getId());
                                if (imgs != null) v.setImagenes(imgs);
                            } catch (Exception ex) {
                                // noop
                            }
                            try {
                                PhysicalPropertiesDTO phys = varianteFisicoService.obtenerPorVarianteId(v.getId());
                                if (phys != null) v.setPhysical(phys);
                            } catch (Exception ex) {
                                // noop
                            }
                        }
                    } catch (Exception ex) {
                        // no fallar toda la respuesta si una variante falla; opcional: loggear
                    }
                }
            }
        } catch (Exception ex) {
            // noop
        } */
        /*
        return dto;
    }
}*/