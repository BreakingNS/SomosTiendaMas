package com.breakingns.SomosTiendaMas.auth.config;

import com.breakingns.SomosTiendaMas.auth.model.Rol;
import com.breakingns.SomosTiendaMas.model.RolNombre;
import com.breakingns.SomosTiendaMas.auth.service.RolService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataLoader {

    @Autowired
    private RolService rolService;

    @PostConstruct
    public void cargarRoles() {
        for (RolNombre nombre : RolNombre.values()) {
            if (rolService.getByNombre(nombre).isEmpty()) {
                rolService.guardar(new Rol(nombre));
            }
        }
    }
}
