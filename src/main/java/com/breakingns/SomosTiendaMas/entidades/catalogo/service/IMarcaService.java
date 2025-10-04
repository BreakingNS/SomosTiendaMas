package com.breakingns.SomosTiendaMas.entidades.catalogo.service;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Marca;

import java.util.List;
import java.util.Optional;

public interface IMarcaService {
    Marca crear(Marca marca);
    Marca actualizar(Long id, Marca cambios);
    Optional<Marca> obtener(Long id);
    Optional<Marca> obtenerPorSlug(String slug);
    List<Marca> listar();
    void eliminarLogico(Long id, String usuario);
}
