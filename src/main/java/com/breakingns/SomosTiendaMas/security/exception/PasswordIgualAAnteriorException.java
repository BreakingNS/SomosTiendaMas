package com.breakingns.SomosTiendaMas.security.exception;

public class PasswordIgualAAnteriorException extends RuntimeException {
    public PasswordIgualAAnteriorException(String mensaje) {
        super(mensaje);
    }
}