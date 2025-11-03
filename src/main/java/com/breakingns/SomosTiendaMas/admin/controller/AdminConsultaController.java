package com.breakingns.SomosTiendaMas.admin.controller;

import com.breakingns.SomosTiendaMas.entidades.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.entidades.perfil_empresa.model.PerfilEmpresa;
import com.breakingns.SomosTiendaMas.entidades.usuario.service.UsuarioServiceImpl;
import com.breakingns.SomosTiendaMas.entidades.perfil_empresa.service.PerfilEmpresaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminConsultaController {

    private final UsuarioServiceImpl usuarioService;
    private final PerfilEmpresaService perfilEmpresaService;

    public AdminConsultaController(UsuarioServiceImpl usuarioService,
                                   PerfilEmpresaService perfilEmpresaService) {
        this.usuarioService = usuarioService;
        this.perfilEmpresaService = perfilEmpresaService;
    }

    @GetMapping("/usuarios") //SOLO TESTEOS ANITA
    public List<Usuario> getAllUsuarios() {
        return usuarioService.traerTodoUsuario();
    }

    @GetMapping("/perfiles-empresa") //SOLO TESTEOS ANITA
    public List<PerfilEmpresa> getAllPerfilesEmpresa() {
        return perfilEmpresaService.traerTodoPerfilEmpresa();
    }
    /*
    @GetMapping("/direcciones") //SOLO TESTEOS ANITA
    public List<DireccionUsuario> getAllDirecciones() {
        return direccionService.traerTodoDireccion();
    }
    
    @GetMapping("/telefonos") //SOLO TESTEOS ANITA
    public List<TelefonoUsuario> getAllTelefonos() {
        return telefonoService.traerTodoTelefono();
    }
        */
}
