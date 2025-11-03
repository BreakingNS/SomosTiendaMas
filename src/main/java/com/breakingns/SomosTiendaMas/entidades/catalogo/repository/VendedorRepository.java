package com.breakingns.SomosTiendaMas.entidades.catalogo.repository;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Vendedor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VendedorRepository extends JpaRepository<Vendedor, Long> {
    Optional<Vendedor> findByUserId(Long userId);
    Optional<Vendedor> findByUserIdAndDeletedAtIsNull(Long userId);

    List<Vendedor> findByNombreContainingIgnoreCase(String nombre);
    List<Vendedor> findAllByDeletedAtIsNullOrderByNombreAsc();

    Optional<Vendedor> findByIdAndDeletedAtIsNull(Long id);

    void deleteByUserId(Long userId);
}