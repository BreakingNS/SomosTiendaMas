package com.breakingns.SomosTiendaMas.entidades.catalogo.service.impl;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_centralizado.PhysicalPropertiesDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Variante;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.VarianteFisico;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.VarianteFisicoRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.VarianteRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IVarianteFisicoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class VarianteFisicoServiceImpl implements IVarianteFisicoService {

    private final VarianteFisicoRepository varianteFisicoRepository;
    private final VarianteRepository varianteRepository;

    public VarianteFisicoServiceImpl(VarianteFisicoRepository varianteFisicoRepository,
                                     VarianteRepository varianteRepository) {
        this.varianteFisicoRepository = varianteFisicoRepository;
        this.varianteRepository = varianteRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PhysicalPropertiesDTO obtenerPorVarianteId(Long varianteId) {
        if (varianteId == null) return null;
        Optional<VarianteFisico> opt = varianteFisicoRepository.findByVariante_IdAndDeletedAtIsNull(varianteId);
        return opt.map(this::toDto).orElse(null);
    }

    @Override
    @Transactional
    public PhysicalPropertiesDTO crearOActualizarPorVariante(Long varianteId, PhysicalPropertiesDTO dto) {
        if (varianteId == null || dto == null) return null;

        Variante variante = varianteRepository.findById(varianteId)
                .orElseThrow(() -> new IllegalArgumentException("Variante no encontrado: " + varianteId));

        VarianteFisico entidad = varianteFisicoRepository.findByVariante_Id(varianteId)
                .orElseGet(() -> {
                    VarianteFisico p = new VarianteFisico();
                    p.setVariante(variante);
                    return p;
                });

        // Mapear campos del DTO a la entidad
        entidad.setWeightGrams(dto.getWeightGrams());
        entidad.setWidthMm(dto.getWidthMm());
        entidad.setHeightMm(dto.getHeightMm());
        entidad.setDepthMm(dto.getDepthMm());
        entidad.setPackageWeightGrams(dto.getPackageWeightGrams());
        entidad.setPackageWidthMm(dto.getPackageWidthMm());
        entidad.setPackageHeightMm(dto.getPackageHeightMm());
        entidad.setPackageDepthMm(dto.getPackageDepthMm());

        VarianteFisico saved = varianteFisicoRepository.save(entidad);
        return toDto(saved);
    }

    @Override
    @Transactional
    public void eliminarPorVarianteId(Long varianteId) {
        if (varianteId == null) return;
        varianteFisicoRepository.findByVariante_Id(varianteId).ifPresent(entity -> {
            varianteFisicoRepository.delete(entity);
        });
    }

    // Mappers
    private PhysicalPropertiesDTO toDto(VarianteFisico e) {
        if (e == null) return null;
        PhysicalPropertiesDTO dto = new PhysicalPropertiesDTO();
        dto.setWeightGrams(e.getWeightGrams());
        dto.setWidthMm(e.getWidthMm());
        dto.setHeightMm(e.getHeightMm());
        dto.setDepthMm(e.getDepthMm());
        dto.setPackageWeightGrams(e.getPackageWeightGrams());
        dto.setPackageWidthMm(e.getPackageWidthMm());
        dto.setPackageHeightMm(e.getPackageHeightMm());
        dto.setPackageDepthMm(e.getPackageDepthMm());
        return dto;
    }
}