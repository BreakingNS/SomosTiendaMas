package com.breakingns.SomosTiendaMas.auth.repository;

import com.breakingns.SomosTiendaMas.auth.model.TokenResetPassword;
import com.breakingns.SomosTiendaMas.domain.usuario.model.Usuario;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IPasswordResetTokenRepository extends JpaRepository<TokenResetPassword, Long> {
    
    Optional<TokenResetPassword> findByToken(String token);
    
    List<TokenResetPassword> findByUsuarioOrderByFechaExpiracionDesc(Usuario usuario); //SOLO PRUEBAS
    
    @Transactional
    void deleteAllByFechaExpiracionBeforeAndUsadoTrue(LocalDateTime cutoff);
    
}
