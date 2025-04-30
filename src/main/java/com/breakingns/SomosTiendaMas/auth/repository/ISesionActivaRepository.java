package com.breakingns.SomosTiendaMas.auth.repository;
  
import com.breakingns.SomosTiendaMas.auth.model.SesionActiva;
import com.breakingns.SomosTiendaMas.domain.usuario.model.Usuario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ISesionActivaRepository extends JpaRepository<SesionActiva, Long> {
    
    List<SesionActiva> findByUsuario_IdUsuario(Long usuarioId);
    
    Optional<SesionActiva> findByToken(String token);

    List<SesionActiva> findByUsuario_IdUsuarioAndRevocadoFalse(Long idUsuario);

    List<SesionActiva> findByUsuario(Usuario usuario);

    List<SesionActiva> findAllByUsuario_UsernameAndRevocadoFalse(String username);

    SesionActiva findByUsuarioAndIpAndUserAgentAndRevocadoFalse(Usuario usuario, String ip, String userAgent);

    List<SesionActiva> findAllByUsuario_IdUsuarioAndRevocadoFalse(Long idUsuario);

}