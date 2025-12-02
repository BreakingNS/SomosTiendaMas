package com.breakingns.SomosTiendaMas.entidades.catalogo.service;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_opcion.ProductoOpcionesAsignarDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_opcion.ProductoConOpcionesDTO;

import java.util.List;

public interface IProductoOpcionService {
    void asignarOpciones(ProductoOpcionesAsignarDTO dto, String usuario);

    ProductoConOpcionesDTO obtenerProductoConOpciones(Long productoId);

    List<ProductoConOpcionesDTO> obtenerTodosConOpciones();

    void modificarOpciones(Long productoId, ProductoOpcionesAsignarDTO dto, String usuario);
    // devuelve por producto las opciones con sus valores (producto_valor si existen, sino valores plantilla)
    com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_opcion.ProductoConOpcionesValoresDTO obtenerProductoConOpcionesConValores(Long productoId);

    // idem para todos los productos
    java.util.List<com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_opcion.ProductoConOpcionesValoresDTO> obtenerTodosConOpcionesConValores();
}