package com.breakingns.SomosTiendaMas.entidades.catalogo.controller;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_valor.ProductoOpcionesValoresAsignarDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_valor.ProductoValoresAsignarDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_valor.ProductoValoresPorProductoDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.mapper.ProductoValorMapper;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IProductoValorService;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.ProductoValorRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.ProductoValor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/productos")
public class ProductoValorController {

    private final IProductoValorService servicio;
    private final ProductoValorRepository repo;

    public ProductoValorController(IProductoValorService servicio, ProductoValorRepository repo) {
        this.servicio = servicio;
        this.repo = repo;
    }

    // agregar valores por opción (bulk nested) -> POST /api/productos/{productoId}/opciones/valores
    @PostMapping("/{productoId}/opciones/valores")
    public ResponseEntity<Void> asignarValoresPorOpciones(@PathVariable Long productoId,
                                                          @RequestBody ProductoOpcionesValoresAsignarDTO dto,
                                                          @RequestHeader(value = "X-User", required = false) String user) {
        if (dto == null || dto.opciones == null) return ResponseEntity.badRequest().build();
        // iterar opciones y valores; reusar servicio existente asignarValor(productoId, valorId)
        for (var opcion : dto.opciones) {
            if (opcion.valores == null) continue;
            for (var v : opcion.valores) {
                try {
                    servicio.asignarValor(productoId, v.id);
                } catch (IllegalStateException ex) {
                    // ya asignado -> ignorar
                }
            }
        }
        return ResponseEntity.ok().build();
    }

    // listar valores de 1 producto
    @GetMapping("/{productoId}/valores")
    public ResponseEntity<List<?>> listarValoresPorProducto(@PathVariable Long productoId) {
        var lista = servicio.listarValoresPorProductoId(productoId);
        return ResponseEntity.ok(lista);
    }

    // listar valores de todos los productos -> devuelve lista de {productoId, valores[]}
    @GetMapping("/valores")
    public ResponseEntity<List<ProductoValoresPorProductoDTO>> listarValoresTodosProductos() {
        List<ProductoValor> relaciones = repo.findAllByDeletedAtIsNull();
        // agrupar por productoId
        Map<Long, List<ProductoValor>> porProducto = relaciones.stream()
                .filter(r -> r.getProducto() != null && r.getProducto().getDeletedAt() == null)
                .collect(Collectors.groupingBy(r -> r.getProducto().getId()));
        List<ProductoValoresPorProductoDTO> salida = porProducto.entrySet().stream()
                .map(e -> ProductoValorMapper.toProductoValoresPorProductoDTO(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(salida);
    }

    // reemplazar valores por opción (estado final) -> PUT /api/productos/{productoId}/opciones/valores
    @PutMapping("/{productoId}/opciones/valores")
    public ResponseEntity<Void> reemplazarValoresPorOpciones(@PathVariable Long productoId,
                                                             @RequestBody ProductoOpcionesValoresAsignarDTO dto,
                                                             @RequestHeader(value = "X-User", required = false) String user) {
        if (dto == null || dto.opciones == null) return ResponseEntity.badRequest().build();
        // obtener valores actuales del producto
        var actuales = servicio.listarValoresPorProductoId(productoId);
        java.util.Set<Long> actualesIds = actuales.stream().map(v -> v.getId()).collect(java.util.stream.Collectors.toSet());

        // construir conjunto de incoming ids
        java.util.Set<Long> incoming = new java.util.HashSet<>();
        for (var opcion : dto.opciones) {
            if (opcion.valores == null) continue;
            for (var v : opcion.valores) incoming.add(v.id);
        }

        // quitar los que están y no vienen
        for (Long idQuitar : new java.util.ArrayList<>(actualesIds)) {
            if (!incoming.contains(idQuitar)) {
                try { servicio.quitarValor(productoId, idQuitar); } catch (Exception ignored) {}
            }
        }
        // agregar los nuevos
        for (Long idAgregar : incoming) {
            if (!actualesIds.contains(idAgregar)) {
                try { servicio.asignarValor(productoId, idAgregar); } catch (Exception ignored) {}
            }
        }
        return ResponseEntity.ok().build();
    }

    // quitar un valor específico
    @DeleteMapping("/{productoId}/valores/{valorId}")
    public ResponseEntity<Void> quitarValor(@PathVariable Long productoId, @PathVariable Long valorId) {
        servicio.quitarValor(productoId, valorId);
        return ResponseEntity.ok().build();
    }
}
