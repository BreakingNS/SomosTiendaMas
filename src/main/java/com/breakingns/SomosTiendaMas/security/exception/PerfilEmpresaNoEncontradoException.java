package com.breakingns.SomosTiendaMas.security.exception;

public class PerfilEmpresaNoEncontradoException extends RuntimeException {
    public PerfilEmpresaNoEncontradoException(Long id) {
        super("Perfil empresa no encontrado con id: " + id);
    }
}