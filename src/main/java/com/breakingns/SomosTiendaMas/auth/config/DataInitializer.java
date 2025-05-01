/*
package com.breakingns.SomosTiendaMas.auth.config;

import com.breakingns.SomosTiendaMas.auth.controller.RegistroController;
import com.breakingns.SomosTiendaMas.domain.usuario.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(3)
public class DataInitializer implements CommandLineRunner {

    private final RegistroController registroController;

    @Autowired
    public DataInitializer(RegistroController registroController) {
        this.registroController = registroController;
    }

    @Override
    public void run(String... args) throws Exception {
        // Intentamos crear un registro admin si no existe ya
        Usuario admin = new Usuario();
        admin.setUsername("adminExample");
        admin.setPassword("admin123"); // Contraseña sin codificar, se codifica en registerAdmin
        admin.setEmail("example@admin.com");

        try {
            registroController.registerAdmin(admin);
            System.out.println("✅ Admin creado correctamente.");
        } catch (Exception e) {
            System.err.println("⚠️ No se pudo crear el admin: " + e.getMessage());
        }
    }
}
*/