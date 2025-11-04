package com.breakingns.SomosTiendaMas.entidades.catalogo.repository;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Opcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OpcionRepository extends JpaRepository<Opcion, Long> {

    // listar plantillas (opciones globales, sin producto directo)
    List<Opcion> findByDeletedAtIsNullOrderByOrdenAsc();

    List<Opcion> findAllByDeletedAtIsNullOrderByOrdenAsc();
    Optional<Opcion> findByIdAndDeletedAtIsNull(Long id);

    List<Opcion> findByNombreContainingIgnoreCase(String nombre);
    List<Opcion> findByTipo(String tipo);

    // obtener máximo orden para calcular el siguiente
    @Query("select max(o.orden) from Opcion o where o.deletedAt is null")
    Optional<Integer> findMaxOrden();
}