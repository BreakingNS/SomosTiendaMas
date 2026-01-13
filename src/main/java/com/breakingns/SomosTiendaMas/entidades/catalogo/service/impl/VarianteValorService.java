package com.breakingns.SomosTiendaMas.entidades.catalogo.service.impl;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.opcion.OpcionValorResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.mapper.OpcionValorMapper;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Variante;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.VarianteValor;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.OpcionValor;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.VarianteValorRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.VarianteRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.OpcionValorRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IVarianteValorService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class VarianteValorService implements IVarianteValorService {

    private final VarianteValorRepository repo;
    private final VarianteRepository varianteRepo;
    private final OpcionValorRepository valorRepo;

    public VarianteValorService(VarianteValorRepository repo,
                                      VarianteRepository varianteRepo,
                                      OpcionValorRepository valorRepo) {
        this.repo = repo;
        this.varianteRepo = varianteRepo;
        this.valorRepo = valorRepo;
    }

    @Override
    public OpcionValorResponseDTO asignarValor(Long varianteId, Long valorId) {
        if (varianteId == null || valorId == null) throw new IllegalArgumentException("varianteId y valorId requeridos");

        Variante variante = varianteRepo.findById(varianteId)
                .orElseThrow(() -> new EntityNotFoundException("Variante no encontrado: " + varianteId));
        OpcionValor valor = valorRepo.findById(valorId)
                .orElseThrow(() -> new EntityNotFoundException("Valor no encontrado: " + valorId));

        // evitar duplicados activos
        repo.findByVarianteIdAndValorId(varianteId, valorId).ifPresent(rel -> {
            if (rel.getDeletedAt() == null) throw new IllegalStateException("Valor ya asignado al variante");
        });

        VarianteValor rel = new VarianteValor();
        rel.setVariante(variante);
        rel.setValor(valor);
        VarianteValor saved = repo.save(rel);
        return OpcionValorMapper.toResponse(saved.getValor());
    }

    @Override
    public void quitarValor(Long varianteId, Long valorId) {
        VarianteValor rel = repo.findByVarianteIdAndValorId(varianteId, valorId)
                .orElseThrow(() -> new EntityNotFoundException("Relación no encontrada"));
        rel.setDeletedAt(LocalDateTime.now());
        repo.save(rel);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OpcionValorResponseDTO> listarValoresPorVarianteId(Long varianteId) {
        List<VarianteValor> list = repo.findByVarianteId(varianteId);
        return list.stream()
                .filter(r -> r.getDeletedAt() == null)
                .map(VarianteValor::getValor)
                .filter(v -> v != null && v.getDeletedAt() == null)
                .map(OpcionValorMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> listarVarianteIdsPorValorId(Long valorId) {
        List<VarianteValor> list = repo.findByValorId(valorId);
        return list.stream()
                .filter(r -> r.getDeletedAt() == null)
                .map(VarianteValor::getVariante)
                .filter(p -> p != null && p.getDeletedAt() == null)
                .map(Variante::getId)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeRelacion(Long varianteId, Long valorId) {
        return repo.findByVarianteIdAndValorId(varianteId, valorId).isPresent();
    }

    @Override
    public void eliminarRelacion(Long id) {
        VarianteValor rel = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Relación no encontrada: " + id));
        rel.setDeletedAt(LocalDateTime.now());
        repo.save(rel);
    }
}