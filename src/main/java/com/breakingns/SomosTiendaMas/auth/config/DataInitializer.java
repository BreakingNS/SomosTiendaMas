package com.breakingns.SomosTiendaMas.auth.config;
/*
import com.breakingns.SomosTiendaMas.auth.repository.IRolRepository;
import com.breakingns.SomosTiendaMas.entidades.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.entidades.usuario.repository.IUsuarioRepository;
import com.breakingns.SomosTiendaMas.entidades.usuario.service.IUsuarioService;
import com.breakingns.SomosTiendaMas.model.RolNombre;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(3)
public class DataInitializer implements CommandLineRunner {

    private final IUsuarioRepository usuarioRepository;
    @Autowired
    private IUsuarioService usuarioService;

    @Autowired
    public DataInitializer(IUsuarioRepository usuarioRepository, IRolRepository rolRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Verificar si ya existe un superadmin
        boolean exists = usuarioRepository.findAll().stream()
            .anyMatch(u -> u.getRol() != null && u.getRol().getNombre() == RolNombre.ROLE_SUPERADMIN);
        if (exists) {
            System.out.println("El superusuario ya existe.");
            return;
        }
        // Crear el superusuario usando el service para asegurar validaciones y password codificada
        Usuario su = new Usuario();
        su.setUsername("superadmin");
        su.setPassword("supersegura123");
        su.setEmail("superadmin@dominio.com");
        su.setActivo(true);
        su.setEmailVerificado(true);
        su.setFechaRegistro(java.time.LocalDateTime.now());
        su.setIntentosFallidosLogin(0);
        su.setCuentaBloqueada(false);
        su.setTipoUsuario(Usuario.TipoUsuario.PERSONA_FISICA);
        su.setNombreResponsable("Super");
        su.setApellidoResponsable("Admin");
        su.setDocumentoResponsable("00000000");
        su.setAceptaTerminos(true);
        su.setAceptaPoliticaPriv(true);
        su.setFechaUltimaModificacion(java.time.LocalDateTime.now());
        usuarioService.registrarConRol(su, RolNombre.ROLE_SUPERADMIN);
        System.out.println("OK Superusuario creado correctamente.");
    }
}*/