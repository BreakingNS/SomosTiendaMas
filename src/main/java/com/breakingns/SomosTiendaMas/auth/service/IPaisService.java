package com.breakingns.SomosTiendaMas.auth.service;

import com.breakingns.SomosTiendaMas.auth.model.Pais;
import java.util.List;

public interface IPaisService {
    long count();
    Pais findByNombreIgnoreCase(String nombre);
    Pais createIfNotExists(String nombre);
    void ensureCountriesExist(List<String> nombres);
}