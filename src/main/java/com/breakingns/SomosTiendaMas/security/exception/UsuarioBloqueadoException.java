package com.breakingns.SomosTiendaMas.security.exception;

public class UsuarioBloqueadoException extends RuntimeException {
    public UsuarioBloqueadoException(String mensaje) {
        super(mensaje);
    }
}