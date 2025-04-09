package com.breakingns.SomosTiendaMas.repository;

import com.breakingns.SomosTiendaMas.model.Carrito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ICarritoRepository extends JpaRepository<Carrito, Long>{
    
}
