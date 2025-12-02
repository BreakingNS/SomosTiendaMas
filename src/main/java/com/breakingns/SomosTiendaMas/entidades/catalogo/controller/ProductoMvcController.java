package com.breakingns.SomosTiendaMas.entidades.catalogo.controller;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.imagen.ImagenProductoDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto.ProductoResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IProductoService;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IImagenProductoService;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Controller
public class ProductoMvcController {

    private final IProductoService productoService;
    private final IImagenProductoService imagenService;

    public ProductoMvcController(IProductoService productoService, IImagenProductoService imagenService) {
        this.productoService = productoService;
        this.imagenService = imagenService;
    }

    @GetMapping("/producto/{id}")
    public String mostrarProducto(@PathVariable Long id, Model model) {
        ProductoResponseDTO producto = productoService.obtenerPorId(id);
        if (producto == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado");
        }

        List<ImagenProductoDTO> imagenes = imagenService.listarPorProductoId(id);

        // garantia adicional: ordenar por 'orden' y poner nulls al final
        imagenes.sort(java.util.Comparator.comparing(
            ImagenProductoDTO::getOrden,
            java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())
        ));

        model.addAttribute("producto", producto);
        model.addAttribute("imagenes", imagenes);
        return "producto/mostrar/producto";
    }

    @GetMapping("/producto/{id}/test-imagenes")
    public String testImagenes(@PathVariable Long id, Model model) {
        ProductoResponseDTO producto = productoService.obtenerPorId(id);
        if (producto == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado");
        }
        List<ImagenProductoDTO> imagenes = imagenService.listarPorProductoId(id);
        imagenes.sort(java.util.Comparator.comparing(
            ImagenProductoDTO::getOrden,
            java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())
        ));
        model.addAttribute("producto", producto);
        model.addAttribute("imagenes", imagenes);
        return "producto/mostrar/test-images";
    }
}