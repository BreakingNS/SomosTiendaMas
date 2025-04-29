package com.breakingns.SomosTiendaMas.security.exception;

public class PasswordIncorrectaException extends RuntimeException {
    public PasswordIncorrectaException(String mensaje) {
        super(mensaje);
    }
}