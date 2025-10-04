package com.breakingns.SomosTiendaMas.entidades.catalogo.service;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Etiqueta;

import java.util.List;
import java.util.Optional;

public interface IEtiquetaService {
    Etiqueta crear(Etiqueta etiqueta);
    List<Etiqueta> listar();
    Optional<Etiqueta> obtener(Long id);
    Etiqueta actualizar(Long id, Etiqueta cambios);
    void eliminarLogico(Long id, String usuario);
    Optional<Etiqueta> findBySlug(String slug);
}