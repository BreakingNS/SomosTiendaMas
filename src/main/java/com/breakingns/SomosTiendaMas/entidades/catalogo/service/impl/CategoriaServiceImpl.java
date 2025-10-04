package com.breakingns.SomosTiendaMas.entidades.catalogo.service.impl;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Categoria;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.CategoriaRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.ICategoriaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CategoriaServiceImpl implements ICategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaServiceImpl(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    public Categoria crear(Categoria categoria) {
        // Si viene categoriaPadre solo con id, cargarla desde repo y asignar
        if (categoria.getCategoriaPadre() != null && categoria.getCategoriaPadre().getId() != null) {
            Long padreId = categoria.getCategoriaPadre().getId();
            Categoria padre = categoriaRepository.findById(padreId)
                    .orElseThrow(() -> new IllegalArgumentException("Categoría padre no encontrada: " + padreId));
            // Para creación no hay id propio todavía, pero evitamos referencia circular directa
            if (categoria.getId() != null && categoria.getId().equals(padre.getId())) {
                throw new IllegalArgumentException("Una categoría no puede ser padre de sí misma");
            }
            categoria.setCategoriaPadre(padre);
        }
        return categoriaRepository.save(categoria);
    }

    @Override
    public Categoria actualizar(Long id, Categoria cambios) {
        Categoria c = categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada: " + id));
        if (cambios.getNombre() != null) c.setNombre(cambios.getNombre());
        if (cambios.getSlug() != null) c.setSlug(cambios.getSlug());
        if (cambios.getDescripcion() != null) c.setDescripcion(cambios.getDescripcion());

        if (cambios.getCategoriaPadre() != null) {
            Long nuevoPadreId = cambios.getCategoriaPadre().getId();
            if (nuevoPadreId == null) {
                // Si el DTO manda padre null-id, interpretamos como quitar padre
                c.setCategoriaPadre(null);
            } else {
                Categoria nuevoPadre = categoriaRepository.findById(nuevoPadreId)
                        .orElseThrow(() -> new IllegalArgumentException("Categoría padre no encontrada: " + nuevoPadreId));
                // validar que no genera ciclos
                validarSinCiclos(c, nuevoPadre);
                c.setCategoriaPadre(nuevoPadre);
            }
        }

        return categoriaRepository.save(c);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Categoria> obtener(Long id) {
        return categoriaRepository.findById(id).filter(c -> c.getDeletedAt() == null);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Categoria> obtenerPorSlug(String slug) {
        return categoriaRepository.findBySlug(slug).filter(c -> c.getDeletedAt() == null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Categoria> listar() {
        return categoriaRepository.findAll().stream().filter(c -> c.getDeletedAt() == null).toList();
    }

    @Override
    public void eliminarLogico(Long id, String usuario) {
        Categoria c = categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada: " + id));
        c.setDeletedAt(LocalDateTime.now());
        c.setUpdatedBy(usuario);
        categoriaRepository.save(c);
    }

    // Nuevo método: asciende por la cadena de padres desde 'nuevoPadre' y verifica que
    // nunca se encuentre la categoría 'categoria' (evita ciclos).
    private void validarSinCiclos(Categoria categoria, Categoria nuevoPadre) {
        if (categoria == null || nuevoPadre == null) return;
        // Si la categoria no tiene id (creación), solo evitamos self-reference directa
        Long categoriaId = categoria.getId();
        Categoria p = nuevoPadre;
        while (p != null) {
            Long pid = p.getId();
            if (categoriaId != null && pid != null && pid.equals(categoriaId)) {
                throw new IllegalArgumentException("Ciclo detectado: el padre pertenece al subárbol de la categoría");
            }
            // subir al padre del candidato
            p = p.getCategoriaPadre();
        }
    }
}