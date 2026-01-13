package com.breakingns.SomosTiendaMas.entidades.catalogo.repository;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.VarianteValor;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Variante;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.OpcionValor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VarianteValorRepository extends JpaRepository<VarianteValor, Long> {
    List<VarianteValor> findByVariante(Variante variante);
    List<VarianteValor> findByValor(OpcionValor valor);

    List<VarianteValor> findByVarianteId(Long varianteId);
    List<VarianteValor> findByValorId(Long valorId);

    Optional<VarianteValor> findByVarianteIdAndValorId(Long varianteId, Long valorId);
    List<VarianteValor> findAllByDeletedAtIsNull();

    void deleteByVarianteId(Long varianteId);
    void deleteByValorId(Long valorId);

    List<VarianteValor> findByVariante_IdAndDeletedAtIsNull(Long varianteId);
}