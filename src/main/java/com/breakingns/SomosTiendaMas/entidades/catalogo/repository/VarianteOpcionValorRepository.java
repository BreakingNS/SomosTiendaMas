package com.breakingns.SomosTiendaMas.entidades.catalogo.repository;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Opcion;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.VarianteOpcionValor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VarianteOpcionValorRepository extends JpaRepository<VarianteOpcionValor, Long> {
    List<VarianteOpcionValor> findByOpcion(Opcion opcion);

    List<VarianteOpcionValor> findByOpcion_IdOrderByOpcionValorOrdenAsc(Long opcionId);

    Optional<VarianteOpcionValor> findByOpcionValor_Slug(String slug);

    Optional<VarianteOpcionValor> findByOpcionValor_SlugAndDeletedAtIsNull(String slug);

    Optional<VarianteOpcionValor> findByOpcion_IdAndOpcionValor_ValorIgnoreCase(Long opcionId, String valor);

    List<VarianteOpcionValor> findAllByDeletedAtIsNullOrderByOpcionValorOrdenAsc();

    Optional<VarianteOpcionValor> findByIdAndDeletedAtIsNull(Long id);

    void deleteByOpcion_Id(Long opcionId);

    List<VarianteOpcionValor> findByOpcion_IdAndDeletedAtIsNull(Long opcionId);
}