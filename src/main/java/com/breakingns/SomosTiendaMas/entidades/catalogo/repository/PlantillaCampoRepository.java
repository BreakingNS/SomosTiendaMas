package com.breakingns.SomosTiendaMas.entidades.catalogo.repository;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.PlantillaCampo;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.PlantillaCategoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlantillaCampoRepository extends JpaRepository<PlantillaCampo, Long> {
    List<PlantillaCampo> findByPlantilla(PlantillaCategoria plantilla);
    List<PlantillaCampo> findByPlantillaIdOrderByOrdenAsc(Long plantillaId);
    Optional<PlantillaCampo> findByPlantillaIdAndSlugIgnoreCase(Long plantillaId, String slug);
    List<PlantillaCampo> findAllByDeletedAtIsNullOrderByOrdenAsc();
}