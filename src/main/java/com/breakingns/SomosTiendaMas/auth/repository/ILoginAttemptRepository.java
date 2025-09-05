package com.breakingns.SomosTiendaMas.auth.repository;

import com.breakingns.SomosTiendaMas.auth.model.LoginAttempt;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ILoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {

    Optional<LoginAttempt> findByUsernameAndIp(String username, String ip);
    
    List<LoginAttempt> findAllByBlockedUntilAfter(LocalDateTime now);
    
    @Transactional
    @Modifying
    @Query("DELETE FROM LoginAttempt i WHERE i.lastAttempt < :fechaLimite")
    int deleteByLastAttemptAntesDe(@Param("fechaLimite") LocalDateTime fechaLimite);
    
    Optional<LoginAttempt> findByUsernameIsNullAndIp(String ip);

    @Query("SELECT i.failedAttempts FROM LoginAttempt i WHERE i.username = :username AND i.ip = :ip")
    Integer findFailedAttemptsByUsernameAndIp(@Param("username") String username, @Param("ip") String ip);
}