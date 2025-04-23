package com.breakingns.SomosTiendaMas.auth.repository;
  
import com.breakingns.SomosTiendaMas.auth.model.SesionActiva;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ISesionActivaRepository extends JpaRepository<SesionActiva, Long> {
    List<SesionActiva> findByUsuario_IdUsuario(Long usuarioId);
    Optional<SesionActiva> findByToken(String token);
}