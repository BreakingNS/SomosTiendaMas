package com.breakingns.SomosTiendaMas.entidades.catalogo.repository;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.VarianteFisico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VarianteFisicoRepository extends JpaRepository<VarianteFisico, Long> {
    Optional<VarianteFisico> findByVariante_Id(Long varianteId);
    Optional<VarianteFisico> findByVariante_IdAndDeletedAtIsNull(Long varianteId);
    void deleteByVariante_Id(Long varianteId);
}