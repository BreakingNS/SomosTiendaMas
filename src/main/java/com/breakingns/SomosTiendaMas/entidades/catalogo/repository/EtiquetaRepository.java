package com.breakingns.SomosTiendaMas.entidades.catalogo.repository;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Etiqueta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EtiquetaRepository extends JpaRepository<Etiqueta, Long> {
    Optional<Etiqueta> findBySlug(String slug);
    Optional<Etiqueta> findBySlugAndDeletedAtIsNull(String slug);

    // === Nuevos métodos ===
    List<Etiqueta> findAllByDeletedAtIsNull();
}
