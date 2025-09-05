package com.breakingns.SomosTiendaMas.admin.controller;

import com.breakingns.SomosTiendaMas.entidades.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.entidades.empresa.model.PerfilEmpresa;
import com.breakingns.SomosTiendaMas.entidades.direccion.model.Direccion;
import com.breakingns.SomosTiendaMas.entidades.telefono.model.Telefono;
import com.breakingns.SomosTiendaMas.entidades.usuario.service.UsuarioServiceImpl;
import com.breakingns.SomosTiendaMas.entidades.empresa.service.PerfilEmpresaService;
import com.breakingns.SomosTiendaMas.entidades.direccion.service.DireccionService;
import com.breakingns.SomosTiendaMas.entidades.telefono.service.TelefonoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminConsultaController {

    private final UsuarioServiceImpl usuarioService;
    private final PerfilEmpresaService perfilEmpresaService;
    private final DireccionService direccionService;
    private final TelefonoService telefonoService;

    public AdminConsultaController(UsuarioServiceImpl usuarioService,
                                   PerfilEmpresaService perfilEmpresaService,
                                   DireccionService direccionService,
                                   TelefonoService telefonoService) {
        this.usuarioService = usuarioService;
        this.perfilEmpresaService = perfilEmpresaService;
        this.direccionService = direccionService;
        this.telefonoService = telefonoService;
    }

    @GetMapping("/usuarios") //SOLO TESTEOS ANITA
    public List<Usuario> getAllUsuarios() {
        return usuarioService.traerTodoUsuario();
    }

    @GetMapping("/perfiles-empresa") //SOLO TESTEOS ANITA
    public List<PerfilEmpresa> getAllPerfilesEmpresa() {
        return perfilEmpresaService.traerTodoPerfilEmpresa();
    }

    @GetMapping("/direcciones") //SOLO TESTEOS ANITA
    public List<Direccion> getAllDirecciones() {
        return direccionService.traerTodoDireccion();
    }

    @GetMapping("/telefonos") //SOLO TESTEOS ANITA
    public List<Telefono> getAllTelefonos() {
        return telefonoService.traerTodoTelefono();
    }
}
