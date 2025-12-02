package com.breakingns.SomosTiendaMas.auth.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaisImportService {

    private final IPaisService paisService;

    public PaisImportService(IPaisService paisService) {
        this.paisService = paisService;
    }

    /**
     * Inserta manualmente los países base si no existen.
     * Ajustá la lista según tu necesidad.
     */
    public void importBaseCountries() {
        List<String> base = List.of("ARGENTINA");
        paisService.ensureCountriesExist(base);
    }
}