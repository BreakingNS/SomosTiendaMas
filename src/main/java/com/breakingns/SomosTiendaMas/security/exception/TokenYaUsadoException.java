package com.breakingns.SomosTiendaMas.security.exception;

public class TokenYaUsadoException extends RuntimeException {
    public TokenYaUsadoException(String mensaje) {
        super(mensaje);
    }
}