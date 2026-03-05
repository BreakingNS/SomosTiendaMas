package com.breakingns.SomosTiendaMas.entidades.catalogo.repository;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Vendedor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VendedorRepository extends JpaRepository<Vendedor, Long> {
    Optional<Vendedor> findByUsuarioId(Long usuarioId);
    Optional<Vendedor> findByUsuarioIdAndDeletedAtIsNull(Long usuarioId);

    Optional<Vendedor> findByEmpresaIdAndDeletedAtIsNull(Long empresaId);
    Optional<Vendedor> findByEmpresaId(Long empresaId);

    List<Vendedor> findByDisplayNameContainingIgnoreCase(String nombre);
    List<Vendedor> findAllByDeletedAtIsNullOrderByDisplayNameAsc();

    Optional<Vendedor> findByIdAndDeletedAtIsNull(Long id);

    boolean existsBySlugAndDeletedAtIsNull(String slug);

    void deleteByUsuarioId(Long usuarioId);
}