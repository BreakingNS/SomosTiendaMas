package com.breakingns.SomosTiendaMas.security.exception;

public class TokenResetPasswordInvalidoException extends RuntimeException {
    public TokenResetPasswordInvalidoException(String mensaje) {
        super(mensaje);
    }
}