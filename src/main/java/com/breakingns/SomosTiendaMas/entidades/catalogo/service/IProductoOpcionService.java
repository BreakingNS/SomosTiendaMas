package com.breakingns.SomosTiendaMas.entidades.catalogo.service;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_opcion.ProductoOpcionesAsignarDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_opcion.ProductoConOpcionesDTO;

import java.util.List;

public interface IProductoOpcionService {
    void asignarOpciones(ProductoOpcionesAsignarDTO dto, String usuario);

    ProductoConOpcionesDTO obtenerProductoConOpciones(Long productoId);

    List<ProductoConOpcionesDTO> obtenerTodosConOpciones();

    void modificarOpciones(Long productoId, ProductoOpcionesAsignarDTO dto, String usuario);
}