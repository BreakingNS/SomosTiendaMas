package com.breakingns.SomosTiendaMas.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.breakingns.SomosTiendaMas.auth.model.Pais;

public interface IPaisRepository extends JpaRepository<Pais, Long> {
    Pais findByNombre(String nombre);
}
