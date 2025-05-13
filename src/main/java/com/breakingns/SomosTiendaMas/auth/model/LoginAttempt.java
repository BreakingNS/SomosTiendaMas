package com.breakingns.SomosTiendaMas.auth.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "login_failedAttempts")
public class LoginAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username; // Puede ser null si querés solo por IP

    private String ip;

    private int failedAttempts;

    private LocalDateTime lastAttempt;

    private LocalDateTime blockedUntil;

    // === Constructores ===
    public LoginAttempt() {}

    public LoginAttempt(String username, String ip, int failedAttempts, LocalDateTime lastAttempt, LocalDateTime blockedUntil) {
        this.username = username;
        this.ip = ip;
        this.failedAttempts = failedAttempts;
        this.lastAttempt = lastAttempt;
        this.blockedUntil = blockedUntil;
    }

    // === Getters y Setters ===
    public Long getId() { return id; }

    public String getUsername() { return username; }

    public void setUsername(String username) { this.username = username; }

    public String getIp() { return ip; }

    public void setIp(String ip) { this.ip = ip; }

    public int getFailedAttempts() { return failedAttempts; }

    public void setFailedAttempts(int failedAttempts) { this.failedAttempts = failedAttempts; }

    public LocalDateTime getLastAttempt() { return lastAttempt; }

    public void setLastAttempt(LocalDateTime lastAttempt) { this.lastAttempt = lastAttempt; }

    public LocalDateTime getBlockedUntil() { return blockedUntil; }

    public void setBlockedUntil(LocalDateTime blockedUntil) { this.blockedUntil = blockedUntil; }
}