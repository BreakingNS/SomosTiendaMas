package com.breakingns.SomosTiendaMas.security.exception;

public class SesionNoEncontradaException extends RuntimeException {
    public SesionNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}