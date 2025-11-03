package com.breakingns.SomosTiendaMas.entidades.catalogo.repository;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Categoria;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.PlantillaCategoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlantillaCategoriaRepository extends JpaRepository<PlantillaCategoria, Long> {
    List<PlantillaCategoria> findByCategoria(Categoria categoria);
    List<PlantillaCategoria> findByCategoriaId(Long categoriaId);
    Optional<PlantillaCategoria> findByCategoriaIdAndDeletedAtIsNull(Long categoriaId);
    List<PlantillaCategoria> findAllByDeletedAtIsNullOrderByCreatedAtAsc();
    Optional<PlantillaCategoria> findByCategoriaIdAndNombreIgnoreCase(Long categoriaId, String nombre);
    Optional<PlantillaCategoria> findByIdAndDeletedAtIsNull(Long id);
    List<PlantillaCategoria> findAllByDeletedAtIsNullOrderByNombreAsc();
}