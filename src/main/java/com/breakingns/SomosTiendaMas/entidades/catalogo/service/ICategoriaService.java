package com.breakingns.SomosTiendaMas.entidades.catalogo.service;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Categoria;

import java.util.List;
import java.util.Optional;

public interface ICategoriaService {
    Categoria crear(Categoria categoria);
    Categoria actualizar(Long id, Categoria cambios);
    Optional<Categoria> obtener(Long id);
    Optional<Categoria> obtenerPorSlug(String slug);
    List<Categoria> listar();
    void eliminarLogico(Long id, String usuario);
}
