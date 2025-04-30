package com.breakingns.SomosTiendaMas.auth.config;

import com.breakingns.SomosTiendaMas.auth.model.Rol;
import com.breakingns.SomosTiendaMas.model.RolNombre;
import com.breakingns.SomosTiendaMas.auth.service.RolService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class DataLoader {

    private final RolService rolService;

    public DataLoader(RolService rolService) {
        this.rolService = rolService;
    }

    @PostConstruct
    public void cargarRoles() {
        for (RolNombre nombre : RolNombre.values()) {
            if (rolService.getByNombre(nombre).isEmpty()) {
                rolService.guardar(new Rol(nombre));
            }
        }
    }
}