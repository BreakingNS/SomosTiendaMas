package com.breakingns.SomosTiendaMas.entidades.catalogo.repository;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Variante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface VarianteRepository extends JpaRepository<Variante, Long> {

    Optional<Variante> findByIdAndProducto_Id(Long id, Long productoId);

    @Query("SELECT v FROM Variante v WHERE v.producto.id = :productoId AND v.attributesHash = :hash")
    Optional<Variante> findByProductoIdAndAttributesHash(@Param("productoId") Long productoId, @Param("hash") String hash);

    @Query("SELECT v FROM Variante v WHERE v.producto.id = :productoId AND v.esDefault = true")
    Optional<Variante> findDefaultByProductoId(@Param("productoId") Long productoId);

    List<Variante> findByProducto_Id(Long productoId);

}
