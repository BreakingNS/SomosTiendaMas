package com.breakingns.SomosTiendaMas.entidades.telefono.repository;

import com.breakingns.SomosTiendaMas.entidades.telefono.model.CodigoArea;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ICodigoAreaRepository extends JpaRepository<CodigoArea, Long> {
    Optional<CodigoArea> findByCodigo(String codigo);
    List<CodigoArea> findAll();
}
