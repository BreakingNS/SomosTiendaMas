package com.breakingns.SomosTiendaMas.security.exception;

public class TokenRevocadoException extends RuntimeException {
    public TokenRevocadoException(String message) {
        super(message);
    }
}