package com.breakingns.SomosTiendaMas.security.exception;

public class SesionNoValidaException extends RuntimeException {
    public SesionNoValidaException(String message) {
        super(message);
    }
}