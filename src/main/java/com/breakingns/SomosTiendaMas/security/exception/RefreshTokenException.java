package com.breakingns.SomosTiendaMas.security.exception;

public class RefreshTokenException extends RuntimeException {
    
    public RefreshTokenException(String mensaje) {
        super(mensaje);
    }

    public RefreshTokenException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
