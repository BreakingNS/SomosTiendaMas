package com.breakingns.SomosTiendaMas.security.exception;

public class SesionActivaNoEncontradaException extends RuntimeException {
    public SesionActivaNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}