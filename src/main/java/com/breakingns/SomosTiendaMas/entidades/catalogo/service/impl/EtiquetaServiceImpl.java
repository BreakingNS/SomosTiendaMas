package com.breakingns.SomosTiendaMas.entidades.catalogo.service.impl;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Etiqueta;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.EtiquetaRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IEtiquetaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EtiquetaServiceImpl implements IEtiquetaService {

    private final EtiquetaRepository repo;

    public EtiquetaServiceImpl(EtiquetaRepository repo) {
        this.repo = repo;
    }

    @Override
    public Etiqueta crear(Etiqueta etiqueta) {
        // simple check: evitar crear slug duplicado activo
        if (etiqueta.getSlug() != null && repo.findBySlugAndDeletedAtIsNull(etiqueta.getSlug()).isPresent()) {
            throw new IllegalArgumentException("Slug de etiqueta ya existe: " + etiqueta.getSlug());
        }
        return repo.save(etiqueta);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Etiqueta> listar() {
        return repo.findAll().stream().filter(e -> e.getDeletedAt() == null).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Etiqueta> obtener(Long id) {
        return repo.findById(id).filter(e -> e.getDeletedAt() == null);
    }

    @Override
    public Etiqueta actualizar(Long id, Etiqueta cambios) {
        Etiqueta e = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Etiqueta no encontrada: " + id));
        if (cambios.getNombre() != null) e.setNombre(cambios.getNombre());
        if (cambios.getSlug() != null) e.setSlug(cambios.getSlug());
        
        return repo.save(e);
    }

    @Override
    public void eliminarLogico(Long id, String usuario) {
        Etiqueta e = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Etiqueta no encontrada: " + id));
        e.setDeletedAt(LocalDateTime.now());
        e.setUpdatedBy(usuario);
        repo.save(e);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Etiqueta> findBySlug(String slug) {
        return repo.findBySlugAndDeletedAtIsNull(slug);
    }
}