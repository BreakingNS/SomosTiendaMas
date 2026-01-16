package com.breakingns.SomosTiendaMas.entidades.catalogo.controllerDesarrollo;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_centralizado.ProductoDetalleResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_opcion.ProductoConOpcionesValoresDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.impl.ProductoAggregatorServiceImpl;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IProductoOpcionService;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IProductoFisicoService;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_centralizado.ProductoDetalleConOpcionesDTO;

@RestController
@RequestMapping("/dev/api/productos")
public class ProductoDetalleControllerDesarrollo {

    private final ProductoAggregatorServiceImpl aggregator;
    private final IProductoOpcionService opcionService;
    private final IProductoFisicoService productoFisicoService;

    public ProductoDetalleControllerDesarrollo(ProductoAggregatorServiceImpl aggregator, IProductoOpcionService opcionService, IProductoFisicoService productoFisicoService) {
        this.aggregator = aggregator;
        this.opcionService = opcionService;
        this.productoFisicoService = productoFisicoService;
    }

    @GetMapping("/{id}/detalle")
    public ResponseEntity<Object> getDetalle(@PathVariable("id") Long id) {
        var detalle = aggregator.buildDetalleById(id);
        if (detalle == null) return ResponseEntity.notFound().build();

        // obtener opciones con sus valores y combinarlas en la misma respuesta
        var opcionesDto = opcionService.obtenerProductoConOpcionesConValores(id);
        if (opcionesDto == null) {
            return ResponseEntity.ok(detalle);
        }
        
        // construir respuesta combinada
        ProductoDetalleConOpcionesDTO out = new ProductoDetalleConOpcionesDTO();

        out.setProducto(detalle.getProducto());
        // REVIEW: se deja de utilizar fisicar para producto, solo para variantes.
        // @Deprecated(since="2026-01-15", forRemoval=true)
        /* 
        out.setImagenes(detalle.getImagenes());
        out.setPrecio(detalle.getPrecio());
        out.setStock(detalle.getStock());
        //obtener físicas desde la tabla producto_fisico
        var physical = productoFisicoService.obtenerPorProductoId(id); 
        out.setPhysical(physical);
        */
        out.setOpciones(opcionesDto.getOpciones());
        
        return ResponseEntity.ok(out);
    }

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
    }
}