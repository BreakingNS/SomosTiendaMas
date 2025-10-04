package com.breakingns.SomosTiendaMas.entidades.catalogo.service.impl;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Marca;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.MarcaRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IMarcaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MarcaServiceImpl implements IMarcaService {

    private final MarcaRepository marcaRepository;

    public MarcaServiceImpl(MarcaRepository marcaRepository) {
        this.marcaRepository = marcaRepository;
    }

    @Override
    public Marca crear(Marca marca) {
        return marcaRepository.save(marca);
    }

    @Override
    public Marca actualizar(Long id, Marca cambios) {
        Marca m = marcaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Marca no encontrada: " + id));
        if (cambios.getNombre() != null) m.setNombre(cambios.getNombre());
        if (cambios.getSlug() != null) m.setSlug(cambios.getSlug());
        if (cambios.getDescripcion() != null) m.setDescripcion(cambios.getDescripcion());
        return marcaRepository.save(m);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Marca> obtener(Long id) {
        return marcaRepository.findById(id).filter(m -> m.getDeletedAt() == null);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Marca> obtenerPorSlug(String slug) {
        return marcaRepository.findBySlug(slug).filter(m -> m.getDeletedAt() == null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Marca> listar() {
        return marcaRepository.findAll().stream().filter(m -> m.getDeletedAt() == null).toList();
    }

    @Override
    public void eliminarLogico(Long id, String usuario) {
        Marca m = marcaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Marca no encontrada: " + id));
        m.setDeletedAt(LocalDateTime.now());
        m.setUpdatedBy(usuario);
        marcaRepository.save(m);
    }
}
