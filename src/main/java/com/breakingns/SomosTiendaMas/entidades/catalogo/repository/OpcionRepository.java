package com.breakingns.SomosTiendaMas.entidades.catalogo.repository;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Opcion;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OpcionRepository extends JpaRepository<Opcion, Long> {
    List<Opcion> findByProducto(Producto producto);

    List<Opcion> findByProductoIdOrderByOrdenAsc(Long productoId);
    List<Opcion> findByProductoIdAndDeletedAtIsNullOrderByOrdenAsc(Long productoId);

    // opciones que pertenecen a una plantilla (producto IS NULL)
    List<Opcion> findByProductoIsNullOrderByOrdenAsc();

    List<Opcion> findAllByDeletedAtIsNullOrderByOrdenAsc();
    Optional<Opcion> findByIdAndDeletedAtIsNull(Long id);

    List<Opcion> findByNombreContainingIgnoreCase(String nombre);
    List<Opcion> findByTipo(String tipo);

    void deleteByProductoId(Long productoId);
}