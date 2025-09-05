package com.breakingns.SomosTiendaMas.auth.config;
/* 
import com.breakingns.SomosTiendaMas.entidades.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.entidades.usuario.service.IUsuarioService;
import com.breakingns.SomosTiendaMas.entidades.usuario.service.UsuarioServiceImpl;
import com.breakingns.SomosTiendaMas.model.RolNombre;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class AdminInitializer implements CommandLineRunner {

    private final IUsuarioService usuarioService;

    public AdminInitializer(UsuarioServiceImpl usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Override
    public void run(String... args) throws Exception {
        // Verificamos si ya existe un admin
        if (usuarioService.existeUsuario("admin") == false) {
            // Crear el usuario admin utilizando el servicio
            Usuario admin = new Usuario();
            admin.setUsername("admin");
            admin.setPassword("987654"); // Cambia la contraseña si lo prefieres
            admin.setEmail("admin@hotmail.com");
            // El rol ADMIN ya está presente en la base de datos, se lo asignamos
            usuarioService.registrarConRol(admin, RolNombre.ROLE_ADMIN);
            System.out.println("Admin creado exitosamente.");
        } else {
            System.out.println("El admin ya existe.");
        }

        //PRUEBA: Crear un usuario común para pruebas
        if (usuarioService.existeUsuario("usuario") == false) {
            // Crear el usuario común utilizando el servicio
            Usuario usuarioComun = new Usuario();
            usuarioComun.setUsername("usuario");
            usuarioComun.setPassword("123456"); // Cambia la contraseña si lo prefieres
            usuarioComun.setEmail("usuario@hotmail.com");
            // El rol USUARIO ya está presente en la base de datos, se lo asignamos
            usuarioService.registrarConRol(usuarioComun, RolNombre.ROLE_USUARIO);
            System.out.println("Usuario común creado exitosamente.");
        } else {
            System.out.println("El usuario común ya existe.");
        }
    }
}*/