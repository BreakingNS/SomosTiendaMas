package com.breakingns.SomosTiendaMas.entidades.catalogo.service;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Producto;

import java.util.List;
import java.util.Optional;

public interface IProductoService {
    Producto crear(Producto producto);
    Producto actualizar(Long id, Producto cambios);
    Optional<Producto> obtener(Long id);
    Optional<Producto> obtenerPorSlug(String slug);
    List<Producto> listar();
    void eliminarLogico(Long id, String usuario);
}
