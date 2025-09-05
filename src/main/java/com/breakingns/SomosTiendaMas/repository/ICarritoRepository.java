package com.breakingns.SomosTiendaMas.repository;

import com.breakingns.SomosTiendaMas.model.Carrito;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ICarritoRepository extends JpaRepository<Carrito, Long>{

    Optional<Carrito> findByUsuario_IdUsuario(Long idUsuario);

    List<Carrito> findAllByUsuario_IdUsuario(Long idUsuario);
}
