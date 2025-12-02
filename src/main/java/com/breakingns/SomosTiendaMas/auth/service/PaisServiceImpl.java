package com.breakingns.SomosTiendaMas.auth.service;

import com.breakingns.SomosTiendaMas.auth.model.Pais;
import com.breakingns.SomosTiendaMas.auth.repository.IPaisRepository;
import com.breakingns.SomosTiendaMas.auth.service.IPaisService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PaisServiceImpl implements IPaisService {

    private final IPaisRepository repo;

    public PaisServiceImpl(IPaisRepository repo) {
        this.repo = repo;
    }

    @Override
    public long count() {
        return repo.count();
    }

    @Override
    public Pais findByNombreIgnoreCase(String nombre) {
        return repo.findByNombreIgnoreCase(nombre);
    }

    @Override
    @Transactional
    public Pais createIfNotExists(String nombre) {
        Pais p = repo.findByNombreIgnoreCase(nombre);
        if (p == null) {
            p = repo.save(new Pais(nombre));
        }
        return p;
    }

    @Override
    @Transactional
    public void ensureCountriesExist(List<String> nombres) {
        for (String n : nombres) {
            createIfNotExists(n);
        }
    }
}