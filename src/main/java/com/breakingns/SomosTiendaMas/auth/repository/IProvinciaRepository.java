package com.breakingns.SomosTiendaMas.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.breakingns.SomosTiendaMas.auth.model.Pais;
import com.breakingns.SomosTiendaMas.auth.model.Provincia;

public interface IProvinciaRepository extends JpaRepository<Provincia, Long> {
    Provincia findByNombreAndPais(String nombre, Pais pais);
    Provincia findByNombre(String nombre);
}
