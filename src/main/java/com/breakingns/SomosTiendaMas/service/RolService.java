package com.breakingns.SomosTiendaMas.service;

import com.breakingns.SomosTiendaMas.model.Rol;
import com.breakingns.SomosTiendaMas.model.RolNombre;
import com.breakingns.SomosTiendaMas.repository.IRolRepository;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RolService {

    @Autowired
    private IRolRepository rolRepository;

    public Optional<Rol> getByNombre(RolNombre nombre) {
        return rolRepository.findByNombre(nombre);
    }

    public void guardar(Rol rol) {
        rolRepository.save(rol);
    }
}
