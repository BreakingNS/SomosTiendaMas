package com.breakingns.SomosTiendaMas.entidades.catalogo.repository;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.ImagenVariante;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Variante;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ImagenVarianteRepository extends JpaRepository<ImagenVariante, Long> {
    // consultas correctas usando la propiedad `variante` y su `id`
    List<ImagenVariante> findByVariante(Variante variante);

    List<ImagenVariante> findByVariante_IdOrderByOrdenAsc(Long varianteId);
    List<ImagenVariante> findByVariante_IdAndDeletedAtIsNullOrderByOrdenAsc(Long varianteId);

    Optional<ImagenVariante> findFirstByVariante_IdOrderByOrdenAsc(Long varianteId);

    List<ImagenVariante> findAllByDeletedAtIsNullOrderByVariante_IdAscOrdenAsc();

    // operaciones útiles
    void deleteByVariante_Id(Long varianteId);
}