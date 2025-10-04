package com.breakingns.SomosTiendaMas.entidades.catalogo.service;

public interface IInventarioService {
    long disponibilidad(Long varianteId);

    // Devuelve true si reservó; usa orderRef para idempotencia
    boolean reservar(Long varianteId, long cantidad, String orderRef);

    // Libera por orderRef; true si liberó algo
    boolean liberar(String orderRef);

    // Confirma venta por orderRef; true si confirmó
    boolean confirmar(String orderRef);

    // Ajustes administrativos (+/- stock físico)
    void ajusteEntrada(Long varianteId, long cantidad, String referencia, String metadataJson);
    void ajusteSalida(Long varianteId, long cantidad, String referencia, String metadataJson);
}
