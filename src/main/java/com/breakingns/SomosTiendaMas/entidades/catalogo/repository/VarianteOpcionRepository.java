package com.breakingns.SomosTiendaMas.entidades.catalogo.repository;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.VarianteOpcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VarianteOpcionRepository extends JpaRepository<VarianteOpcion, Long> {

    List<VarianteOpcion> findByVariante_Producto_IdAndDeletedAtIsNullOrderByOrdenAsc(Long productoId);

    List<VarianteOpcion> findByVariante_IdAndDeletedAtIsNullOrderByOrdenAsc(Long varianteId);

    boolean existsByVariante_Producto_IdAndOpcion_IdAndDeletedAtIsNull(Long productoId, Long opcionId);

    Optional<VarianteOpcion> findByVariante_Producto_IdAndOpcion_IdAndDeletedAtIsNull(Long productoId, Long opcionId);

    void deleteByOpcion_Id(Long opcionId);

    @Query("select max(vo.orden) from VarianteOpcion vo where vo.variante.producto.id = :productoId and vo.deletedAt is null")
    Optional<Integer> findMaxOrdenByProductoId(@Param("productoId") Long productoId);

}
