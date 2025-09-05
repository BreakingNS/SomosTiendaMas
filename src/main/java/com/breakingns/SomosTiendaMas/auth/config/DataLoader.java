package com.breakingns.SomosTiendaMas.auth.config;

import com.breakingns.SomosTiendaMas.auth.model.Rol;
import com.breakingns.SomosTiendaMas.auth.model.RolNombre;
import com.breakingns.SomosTiendaMas.auth.repository.IRolRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class DataLoader implements CommandLineRunner {

    private final IRolRepository rolRepository;

    public DataLoader(IRolRepository rolRepository) {
        this.rolRepository = rolRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (rolRepository.count() == 0) {
            // Cargar roles si no existen
            rolRepository.save(new Rol(RolNombre.ROLE_SUPERADMIN));
            rolRepository.save(new Rol(RolNombre.ROLE_ADMIN));
            rolRepository.save(new Rol(RolNombre.ROLE_EMPRESA));
            rolRepository.save(new Rol(RolNombre.ROLE_MODERADOR));
            rolRepository.save(new Rol(RolNombre.ROLE_SOPORTE));
            rolRepository.save(new Rol(RolNombre.ROLE_ANALISTA));
            rolRepository.save(new Rol(RolNombre.ROLE_USUARIO));
            System.out.println("Roles cargados correctamente.");
        } else {
            System.out.println("Los roles ya existen.");
        }
    }
}