package com.breakingns.SomosTiendaMas.repository;

import com.breakingns.SomosTiendaMas.model.Rol;
import com.breakingns.SomosTiendaMas.model.RolNombre;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IRolRepository extends JpaRepository<Rol, Long> {
    Optional<Rol> findByNombre(RolNombre nombre);
}
