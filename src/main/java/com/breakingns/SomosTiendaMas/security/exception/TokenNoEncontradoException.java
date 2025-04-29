package com.breakingns.SomosTiendaMas.security.exception;

public class TokenNoEncontradoException extends RuntimeException {
    public TokenNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}