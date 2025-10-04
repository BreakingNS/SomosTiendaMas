package com.breakingns.SomosTiendaMas.entidades.catalogo.service;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.OpcionProducto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.ValorOpcionProducto;

import java.util.List;

public interface IOpcionProductoService {
    OpcionProducto crearOpcion(Long productoId, String nombre, Integer orden);
    OpcionProducto actualizarOpcion(Long opcionId, String nombre, Integer orden);
    void eliminarOpcion(Long opcionId);
    List<OpcionProducto> listarOpciones(Long productoId);

    ValorOpcionProducto crearValor(Long opcionId, String valor, String slug, Integer orden);
    void eliminarValor(Long valorId);
    List<ValorOpcionProducto> listarValores(Long opcionId);
}
