package com.breakingns.SomosTiendaMas.entidades.catalogo.controllerDesarrollo;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_opcion.ProductoConOpcionesDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_opcion.ProductoConOpcionesValoresDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_opcion.ProductoOpcionesAsignarDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IProductoOpcionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dev/api/productos")
public class ProductoOpcionControllerDesarrollo {
    private final IProductoOpcionService service;
    public ProductoOpcionControllerDesarrollo(IProductoOpcionService service) { this.service = service; }

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

    // nuevo: obtener un producto con sus opciones y los valores (producto_valor o plantilla)
    @GetMapping("/{productoId}/con-opciones-valores")
    public ResponseEntity<ProductoConOpcionesValoresDTO> obtenerProductoConOpcionesConValores(@PathVariable Long productoId) {
        var res = service.obtenerProductoConOpcionesConValores(productoId);
        return ResponseEntity.ok(res);
    }

    // nuevo: obtener todos los productos con opciones y valores
    @GetMapping("/con-opciones-valores")
    public ResponseEntity<List<ProductoConOpcionesValoresDTO>> obtenerTodosConOpcionesConValores() {
        var res = service.obtenerTodosConOpcionesConValores();
        return ResponseEntity.ok(res);
    }

    // nuevo: modificar (agregar / borrar / actualizar) las opciones de un producto
    @PutMapping("/{productoId}/opciones")
    public ResponseEntity<Void> modificarOpciones(@PathVariable Long productoId, @RequestBody ProductoOpcionesAsignarDTO dto, @RequestHeader(value = "X-User", required = false) String user) {
        service.modificarOpciones(productoId, dto, user != null ? user : "system");
        return ResponseEntity.ok().build();
    }
}