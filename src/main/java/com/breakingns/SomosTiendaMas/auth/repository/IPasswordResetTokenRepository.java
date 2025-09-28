package com.breakingns.SomosTiendaMas.auth.repository;

import com.breakingns.SomosTiendaMas.auth.model.TokenResetPassword;
import com.breakingns.SomosTiendaMas.entidades.usuario.model.Usuario;

import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface IPasswordResetTokenRepository extends JpaRepository<TokenResetPassword, Long> {
    
    Optional<TokenResetPassword> findByToken(String token);
    
    List<TokenResetPassword> findByUsuarioOrderByFechaExpiracionDesc(Usuario usuario); //SOLO PRUEBAS
    
    @Transactional
    void deleteAllByFechaExpiracionBeforeAndUsadoTrue(LocalDateTime cutoff);
    
    Optional<TokenResetPassword> findByUsuario_IdUsuarioAndUsadoFalse(Long usuarioId);

    @Modifying
    @Transactional
    @Query("UPDATE TokenResetPassword t SET t.usado = true WHERE t.token = :token AND t.usado = false AND t.fechaExpiracion > :now")
    int markAsUsedIfValid(@Param("token") String token, @Param("now") Instant now);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TokenResetPassword t WHERE t.token = :token")
    Optional<TokenResetPassword> findByTokenForUpdate(@Param("token") String token);

    @Modifying
    @Transactional
    @Query("update TokenResetPassword t set t.usado = true where t.token = :token and t.usado = false and (t.fechaExpiracion is null or t.fechaExpiracion > current_timestamp)")
    int markAsUsedIfValid(@Param("token") String token);

}
