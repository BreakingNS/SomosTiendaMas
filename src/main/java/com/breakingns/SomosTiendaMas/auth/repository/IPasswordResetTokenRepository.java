package com.breakingns.SomosTiendaMas.auth.repository;

import com.breakingns.SomosTiendaMas.auth.model.TokenResetPassword;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IPasswordResetTokenRepository extends JpaRepository<TokenResetPassword, Long> {
    
    Optional<TokenResetPassword> findByToken(String token);

}
