package com.breakingns.SomosTiendaMas.auth.service;

import com.breakingns.SomosTiendaMas.auth.model.Rol;
import com.breakingns.SomosTiendaMas.auth.model.RolNombre;
import com.breakingns.SomosTiendaMas.auth.repository.IRolRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class RolService {

    private final IRolRepository rolRepository;

    public RolService(IRolRepository rolRepository) {
        this.rolRepository = rolRepository;
    }

    public Optional<Rol> getByNombre(RolNombre nombre) {
        return rolRepository.findByNombre(nombre);
    }

    public void guardar(Rol rol) {
        rolRepository.save(rol);
    }
    
    public List<String> traerRoles() {
        List<Rol> roles = rolRepository.findAll();
        return roles.stream()
                    .map(rol -> rol.getNombre().name()) // Convertir RolNombre a String
                    .collect(Collectors.toList());
    }
}
