package com.breakingns.SomosTiendaMas.entidades.catalogo.service.impl;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.variante1.VarianteCrearDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.variante1.VarianteDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.mapper.VarianteMapper;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Producto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Variante;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.ProductoRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.VarianteRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IVarianteService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.Optional;

@Service
@Transactional
public class VarianteService implements IVarianteService {

    private final VarianteRepository varianteRepo;
    private final ProductoRepository productoRepo;

    public VarianteService(VarianteRepository varianteRepo, ProductoRepository productoRepo) {
        this.varianteRepo = varianteRepo;
        this.productoRepo = productoRepo;
    }

    @Override
    public VarianteDTO crearVariante(VarianteCrearDTO dto) {
        if (dto == null) throw new IllegalArgumentException("dto null");

        Producto producto = null;
        if (dto.getProductoId() != null) {
            producto = productoRepo.findById(dto.getProductoId()).orElseThrow(() -> new EntityNotFoundException("Producto no encontrado: " + dto.getProductoId()));
        } else {
            throw new IllegalArgumentException("productoId es requerido");
        }

        // comprobar unicidad por attributes_hash si disponible
        if (dto.getAttributesHash() != null) {
            Optional<Variante> existing = varianteRepo.findByProductoIdAndAttributesHash(producto.getId(), dto.getAttributesHash());
            if (existing.isPresent()) throw new IllegalStateException("Variante con same attributes_hash ya existe para el producto");
        }

        Variante v = VarianteMapper.fromCrearDto(dto, producto);
        Variante saved = varianteRepo.save(v);
        // si es default y producto no tiene default, se mantiene; si se marca default y ya existía uno, no lo alteramos aquí
        return VarianteMapper.toDto(saved);
    }

    @Override
    public VarianteDTO actualizar(Long id, VarianteCrearDTO dto) {
        Variante v = varianteRepo.findById(id).orElseThrow(() -> new EntityNotFoundException("Variante no encontrada: " + id));
        if (dto.getSku() != null) v.setSku(dto.getSku());
        if (dto.getAttributesJson() != null) v.setAttributesJson(dto.getAttributesJson());
        if (dto.getAttributesHash() != null) v.setAttributesHash(dto.getAttributesHash());
        if (dto.getEsDefault() != null) v.setEsDefault(dto.getEsDefault());
        if (dto.getActivo() != null) v.setActivo(dto.getActivo());
        Variante saved = varianteRepo.save(v);
        return VarianteMapper.toDto(saved);
    }

    @Override
    public void eliminar(Long id) {
        Variante v = varianteRepo.findById(id).orElseThrow(() -> new EntityNotFoundException("Variante no encontrada: " + id));
        v.markDeleted("system");
        varianteRepo.save(v);
    }

    @Override
    public java.util.List<VarianteDTO> crearVariantesBatch(Long productoId, java.util.List<VarianteCrearDTO> dtos) {
        if (productoId == null) throw new IllegalArgumentException("productoId required");
        java.util.List<VarianteDTO> out = new java.util.ArrayList<>();
        for (VarianteCrearDTO dto : dtos) {
            if (dto == null) continue;
            dto.setProductoId(productoId);
            // calcular hash si falta
            if ((dto.getAttributesHash() == null || dto.getAttributesHash().isBlank()) && dto.getAttributesJson() != null) {
                dto.setAttributesHash(calculateSha256Hex(dto.getAttributesJson()));
            }
            VarianteDTO created = crearVariante(dto);
            out.add(created);
        }
        return out;
    }

    @Override
    @Transactional(readOnly = true)
    public VarianteDTO obtenerPorId(Long id) {
        Variante v = varianteRepo.findById(id).orElseThrow(() -> new EntityNotFoundException("Variante no encontrada: " + id));
        return VarianteMapper.toDto(v);
    }

    @Override
    @Transactional(readOnly = true)
    public VarianteDTO obtenerDefaultByProductoId(Long productoId) {
        if (productoId == null) return null;
        var opt = varianteRepo.findDefaultByProductoId(productoId);
        return opt.map(VarianteMapper::toDto).orElse(null);
    }

    // helper para calcular SHA-256 hex de un texto
    private String calculateSha256Hex(String text) {
        if (text == null) return null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
