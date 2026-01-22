// NOTE: deprecado, ahora se utiliza ProductoCentralizadoControllerDesarrollo
/* 
package com.breakingns.SomosTiendaMas.entidades.catalogo.controllerDesarrollo;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.breakingns.SomosTiendaMas.entidades.catalogo.service.impl.ProductoAggregatorServiceImpl;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_public.ProductoCentralizadoPublicDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_public.VarianteCentralPublicDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_public.PrecioPublicDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_public.DisponibilidadPublicDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_public.OpcionPublicDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_public.ValorPublicDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_public.ImagenPublicDTO;


@RestController
@RequestMapping("/dev/api/productos")
public class ProductoDetalleControllerDesarrollo {

    private final ProductoAggregatorServiceImpl aggregator;

    public ProductoDetalleControllerDesarrollo(ProductoAggregatorServiceImpl aggregator) {
        this.aggregator = aggregator;
    }

    @GetMapping("/{id}/productoCentralizado")
    public ResponseEntity<ProductoCentralizadoPublicDTO> getProductoSolo(@PathVariable("id") Long id) {
        var detalle = aggregator.buildDetalleById(id);
        if (detalle == null) return ResponseEntity.notFound().build();
        var producto = detalle.getProducto();
        if (producto == null) return ResponseEntity.notFound().build();

        ProductoCentralizadoPublicDTO out = new ProductoCentralizadoPublicDTO();
        out.setId(producto.getId());
        out.setNombre(producto.getNombre());
        out.setSlug(producto.getSlug());
        out.setDescripcion(producto.getDescripcion());
        out.setMarcaNombre(producto.getMarcaNombre());
        out.setNombreCategoriaPadre(producto.getNombreCategoriaPadre());
        out.setNombreCategoriaHija(producto.getNombreCategoriaHija());
        out.setSku(producto.getSku());
        out.setCondicion(producto.getCondicion() != null ? producto.getCondicion().name() : null);
        out.setGarantia(producto.getGarantia());
        out.setPoliticaDevoluciones(producto.getPoliticaDevoluciones());*/
        //out.setSkuResuelto(producto.getSkuResuelto());
        /* 
        if (producto.getVariantes() != null) {
            var list = new java.util.ArrayList<VarianteCentralPublicDTO>();
            for (var v : producto.getVariantes()) {
                VarianteCentralPublicDTO vp = new VarianteCentralPublicDTO();
                vp.setId(v.getId());
                vp.setProductoId(v.getProductoId());
                vp.setSkuResuelto(v.getSkuResuelto());

                if (v.getPrecio() != null) {
                    PrecioPublicDTO pp = new PrecioPublicDTO();
                    pp.setMontoCentavos(v.getPrecio().getMontoCentavos());
                    pp.setPrecioAnteriorCentavos(v.getPrecio().getPrecioAnteriorCentavos());
                    pp.setDescuentoPorcentaje(v.getPrecio().getDescuentoPorcentaje());
                    pp.setMoneda(v.getPrecio().getMoneda());
                    pp.setPrecioSinIvaCentavos(v.getPrecio().getPrecioSinIvaCentavos());
                    pp.setIvaPorcentaje(v.getPrecio().getIvaPorcentaje());
                    pp.setVigenciaDesde(v.getPrecio().getVigenciaDesde());
                    pp.setVigenciaHasta(v.getPrecio().getVigenciaHasta());
                    vp.setPrecio(pp);
                }

                if (v.getDisponible() != null) {
                    DisponibilidadPublicDTO dp = new DisponibilidadPublicDTO();
                    dp.setVarianteId(v.getDisponible().getVarianteId());
                    dp.setDisponible(v.getDisponible().getDisponible());
                    vp.setDisponible(dp);
                }

                vp.setEsDefault(v.getEsDefault());
                vp.setActivo(v.getActivo());

                if (v.getOpciones() != null) {
                    var opts = new java.util.ArrayList<OpcionPublicDTO>();
                    for (var o : v.getOpciones()) {
                        OpcionPublicDTO op = new OpcionPublicDTO();
                        op.setNombre(o.getNombre());
                        op.setOrden(o.getOrden());
                        if (o.getValores() != null) {
                            var vals = new java.util.ArrayList<ValorPublicDTO>();
                            for (var val : o.getValores()) {
                                ValorPublicDTO vpv = new ValorPublicDTO();
                                vpv.setValor(val.getValor());
                                vpv.setOrden(val.getOrden());
                                vals.add(vpv);
                            }
                            op.setValores(vals);
                        }
                        opts.add(op);
                    }
                    vp.setOpciones(opts);
                }

                if (v.getImagenes() != null) {
                    var imgs = new java.util.ArrayList<ImagenPublicDTO>();
                    for (var im : v.getImagenes()) {
                        ImagenPublicDTO ip = new ImagenPublicDTO();
                        ip.setUrl(im.getUrl());
                        ip.setAlt(im.getAlt());
                        ip.setOrden(im.getOrden());
                        imgs.add(ip);
                    }
                    vp.setImagenes(imgs);
                }

                vp.setPhysical(v.getPhysical());

                list.add(vp);
            }
            out.setVariantes(list);
        }
        */
        //return ResponseEntity.ok(out);
    //}
    /*
    // nuevo: detalle que incluye opciones y valores (producto_opcion / producto_valor)
    @GetMapping("/{id}/detalle-con-opciones")
    public ResponseEntity<ProductoConOpcionesValoresDTO> getDetalleConOpciones(@PathVariable("id") Long id) {
        var dto = opcionService.obtenerProductoConOpcionesConValores(id);
        if (dto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/detalle")
    public ResponseEntity<List<ProductoDetalleResponseDTO>> getTodosDetalle() {
        var lista = aggregator.buildDetallesAll();
        return ResponseEntity.ok(lista);
    }*/
//}