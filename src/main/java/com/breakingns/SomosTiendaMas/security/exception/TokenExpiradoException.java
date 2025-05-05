package com.breakingns.SomosTiendaMas.security.exception;

public class TokenExpiradoException extends RuntimeException {
    public TokenExpiradoException(String mensaje) {
        super(mensaje);
    }
}