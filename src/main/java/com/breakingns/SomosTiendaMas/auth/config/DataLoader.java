package com.breakingns.SomosTiendaMas.auth.config;

import com.breakingns.SomosTiendaMas.auth.model.Rol;
import com.breakingns.SomosTiendaMas.auth.repository.IRolRepository;
import com.breakingns.SomosTiendaMas.model.RolNombre;
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
            Rol adminRole = new Rol(RolNombre.ROLE_ADMIN);
            Rol userRole = new Rol(RolNombre.ROLE_USUARIO);
            rolRepository.save(adminRole);
            rolRepository.save(userRole);
            System.out.println("Roles cargados correctamente.");
        } else {
            System.out.println("Los roles ya existen.");
        }
    }
}