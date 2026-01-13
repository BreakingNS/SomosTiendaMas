package com.breakingns.SomosTiendaMas.entidades.catalogo.repository;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.ProductDraft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductDraftRepository extends JpaRepository<ProductDraft, Long> {
    List<ProductDraft> findByOwnerId(String ownerId);
    List<ProductDraft> findByOwnerIdAndStatus(String ownerId, String status);
}