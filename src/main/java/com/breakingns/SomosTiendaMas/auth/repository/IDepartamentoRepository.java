package com.breakingns.SomosTiendaMas.auth.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.breakingns.SomosTiendaMas.auth.model.Departamento;
import com.breakingns.SomosTiendaMas.auth.model.Provincia;

public interface IDepartamentoRepository extends JpaRepository<Departamento, Long> {
    Departamento findByNombreAndProvincia(String nombre, Provincia provincia);
    Departamento findByNombre(String nombre);
    List<Departamento> findByProvinciaId(Long provinciaId);
}