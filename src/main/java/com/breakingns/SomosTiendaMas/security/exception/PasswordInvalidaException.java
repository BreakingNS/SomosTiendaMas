package com.breakingns.SomosTiendaMas.security.exception;

public class PasswordInvalidaException extends RuntimeException {
    public PasswordInvalidaException(String message) {
        super(message);
    }
}