package com.breakingns.SomosTiendaMas.entidades.catalogo.repository;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.OpcionValor;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Opcion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OpcionValorRepository extends JpaRepository<OpcionValor, Long> {
    List<OpcionValor> findByOpcion(Opcion opcion);
    List<OpcionValor> findByOpcionIdOrderByOrdenAsc(Long opcionId);

    Optional<OpcionValor> findBySlug(String slug);
    Optional<OpcionValor> findByOpcionIdAndValorIgnoreCase(Long opcionId, String valor);

    List<OpcionValor> findAllByDeletedAtIsNullOrderByOrdenAsc();
    Optional<OpcionValor> findByIdAndDeletedAtIsNull(Long id);

    void deleteByOpcionId(Long opcionId);
}