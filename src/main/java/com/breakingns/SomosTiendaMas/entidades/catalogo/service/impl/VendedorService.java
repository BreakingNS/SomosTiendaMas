package com.breakingns.SomosTiendaMas.entidades.catalogo.service.impl;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.vendedor.*;
import com.breakingns.SomosTiendaMas.entidades.catalogo.mapper.VendedorMapper;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Vendedor;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.VendedorRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IVendedorService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class VendedorService implements IVendedorService {

    private final VendedorRepository repo;

    public VendedorService(VendedorRepository repo) {
        this.repo = repo;
    }

    @Override
    public VendedorResponseDTO crear(VendedorCrearDTO dto) {
        if (dto == null) throw new IllegalArgumentException("dto es null");
        if (dto.getUserId() != null && repo.findByUserIdAndDeletedAtIsNull(dto.getUserId()).isPresent()) {
            throw new IllegalStateException("Ya existe un vendedor para ese userId");
        }
        Vendedor entidad = VendedorMapper.fromCrear(dto);
        Vendedor saved = repo.save(entidad);
        return VendedorMapper.toResponse(saved);
    }

    @Override
    public VendedorResponseDTO actualizar(Long id, VendedorActualizarDTO dto) {
        Vendedor v = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Vendedor no encontrado: " + id));
        VendedorMapper.applyActualizar(dto, v);
        Vendedor updated = repo.save(v);
        return VendedorMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public VendedorResponseDTO obtenerPorId(Long id) {
        Vendedor v = repo.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new EntityNotFoundException("Vendedor no encontrado: " + id));
        return VendedorMapper.toResponse(v);
    }

    @Override
    @Transactional(readOnly = true)
    public VendedorResponseDTO obtenerPorUserId(Long userId) {
        Vendedor v = repo.findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new EntityNotFoundException("Vendedor no encontrado para userId: " + userId));
        return VendedorMapper.toResponse(v);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VendedorResumenDTO> listarActivos() {
        List<Vendedor> list = repo.findAllByDeletedAtIsNullOrderByNombreAsc();
        return list.stream().map(VendedorMapper::toResumen).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VendedorResumenDTO> buscarPorFiltro(VendedorFiltroDTO filtro) {
        if (filtro == null || filtro.getNombreContains() == null) return listarActivos();
        List<Vendedor> list = repo.findByNombreContainingIgnoreCase(filtro.getNombreContains());
        return list.stream()
                .filter(v -> v.getDeletedAt() == null)
                .map(VendedorMapper::toResumen)
                .collect(Collectors.toList());
    }

    @Override
    public void eliminar(Long id) {
        Vendedor v = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Vendedor no encontrado: " + id));
        v.setDeletedAt(LocalDateTime.now());
        repo.save(v);
    }

    @Override
    public void eliminarPorUserId(Long userId) {
        if (userId == null) return;
        repo.deleteByUserId(userId);
    }
}