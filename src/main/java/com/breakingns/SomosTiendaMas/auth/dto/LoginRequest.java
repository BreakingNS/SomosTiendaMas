package com.breakingns.SomosTiendaMas.auth.dto;

public record LoginRequest (
        String username, 
        String password
) {}