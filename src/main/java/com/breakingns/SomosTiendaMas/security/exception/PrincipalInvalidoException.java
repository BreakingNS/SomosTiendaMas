package com.breakingns.SomosTiendaMas.security.exception;

public class PrincipalInvalidoException extends RuntimeException {
    public PrincipalInvalidoException(String mensaje) {
        super(mensaje);
    }
}