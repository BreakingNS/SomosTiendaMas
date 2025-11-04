package com.breakingns.SomosTiendaMas.entidades.catalogo.controller;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_opcion.ProductoConOpcionesDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_opcion.ProductoOpcionesAsignarDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IProductoOpcionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoOpcionController {
    private final IProductoOpcionService service;
    public ProductoOpcionController(IProductoOpcionService service) { this.service = service; }

    @PostMapping("/{productoId}/opciones/asignar")
    public ResponseEntity<Void> asignarOpciones(@PathVariable Long productoId, @RequestBody ProductoOpcionesAsignarDTO dto, @RequestHeader(value = "X-User", required = false) String user) {
         dto.productoId = productoId;
         service.asignarOpciones(dto, user != null ? user : "system");
         return ResponseEntity.ok().build();
     }

    // nuevo: obtener un producto con sus opciones
    @GetMapping("/{productoId}/con-opciones")
    public ResponseEntity<ProductoConOpcionesDTO> obtenerProductoConOpciones(@PathVariable Long productoId) {
        var res = service.obtenerProductoConOpciones(productoId);
        return ResponseEntity.ok(res);
    }

    // nuevo: obtener todos los productos con sus opciones (paginar si hace falta)
    @GetMapping("/con-opciones")
    public ResponseEntity<List<ProductoConOpcionesDTO>> obtenerTodosConOpciones() {
        var res = service.obtenerTodosConOpciones();
        return ResponseEntity.ok(res);
    }

    // nuevo: modificar (agregar / borrar / actualizar) las opciones de un producto
    @PutMapping("/{productoId}/opciones")
    public ResponseEntity<Void> modificarOpciones(@PathVariable Long productoId, @RequestBody ProductoOpcionesAsignarDTO dto, @RequestHeader(value = "X-User", required = false) String user) {
        service.modificarOpciones(productoId, dto, user != null ? user : "system");
        return ResponseEntity.ok().build();
    }
}