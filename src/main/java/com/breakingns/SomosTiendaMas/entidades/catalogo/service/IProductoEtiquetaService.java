package com.breakingns.SomosTiendaMas.entidades.catalogo.service;

public interface IProductoEtiquetaService {
    void asignarEtiqueta(Long productoId, Long etiquetaId);
    void quitarEtiqueta(Long productoId, Long etiquetaId);
}