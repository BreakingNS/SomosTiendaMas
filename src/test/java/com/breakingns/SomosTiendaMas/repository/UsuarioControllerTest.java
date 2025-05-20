package com.breakingns.SomosTiendaMas.repository;

import com.breakingns.SomosTiendaMas.auth.controller.RegistroController;
import com.breakingns.SomosTiendaMas.auth.dto.shared.RegistroUsuarioDTO;
import com.breakingns.SomosTiendaMas.auth.service.LoginAttemptService;
import com.breakingns.SomosTiendaMas.auth.service.RolService;
import com.breakingns.SomosTiendaMas.domain.usuario.service.UsuarioServiceImpl;
import com.breakingns.SomosTiendaMas.service.CarritoService;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RegistroController.class)
@AutoConfigureMockMvc(addFilters = false) // opcional si no necesitás filtros de seguridad en el test
class UsuarioControllerTest {

    @MockBean
    private RolService rolService;

    @MockBean
    private LoginAttemptService loginAttemptService;

    @MockBean
    private UsuarioServiceImpl usuarioService;
    
    @MockBean
    private CarritoService carritoService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registroLanzaExcepcionInterna_devuelveInternalServerError() throws Exception {
        RegistroUsuarioDTO dtoValido = new RegistroUsuarioDTO("usuarioValido", "email@valido.com", "123456");

        Mockito.doThrow(new RuntimeException("Error interno en el servidor"))
                .when(usuarioService).registrarConRolDesdeDTO(any(), any());

        mockMvc.perform(post("/api/registro/public/usuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dtoValido)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Error interno en el servidor"));
    }
}

