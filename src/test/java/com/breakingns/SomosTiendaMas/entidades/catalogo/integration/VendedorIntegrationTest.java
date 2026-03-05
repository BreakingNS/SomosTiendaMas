package com.breakingns.SomosTiendaMas.entidades.catalogo.integration;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Vendedor;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.VendedorRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.breakingns.SomosTiendaMas.security.filter.JwtAuthenticationFilter;
import jakarta.persistence.EntityManager;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc(addFilters = false)
@Transactional
public class VendedorIntegrationTest {
    
    @Autowired
    private VendedorRepository repository;

    @Autowired
    private MockMvc restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private EntityManager entityManager;
    
    // === TESTS DE REPOSITORY ===
    
    @Test
    void save_DeberiaCrearVendedor_CuandoDatosValidos() {
        // Crear entidad con datos válidos
        Vendedor vendedor = new Vendedor();
        vendedor.setUsuarioId(1L);
        vendedor.setNombreLegal("Tienda Test S.R.L.");
        vendedor.setDisplayName("Tienda Test");
        vendedor.setSlug("tienda-test-1");
        vendedor.setStatus(Vendedor.EstatusVendedor.ACTIVO);
         
        // Guardar en DB
        Vendedor guardado = repository.save(vendedor);
        
        // Verificar que ID fue generado
        assertNotNull(guardado.getId(), "El ID debe ser generado automáticamente");
        
        // Verificar que los datos se guardaron correctamente
        assertEquals(1L, guardado.getUsuarioId());
        assertEquals("Tienda Test S.R.L.", guardado.getNombreLegal());
        assertEquals("Tienda Test", guardado.getDisplayName());
        assertEquals(Vendedor.EstatusVendedor.ACTIVO, guardado.getStatus());
    }
    
    @Test
    void findById_DeberiaRetornarVendedor_CuandoExiste() {
        // Guardar entidad en DB
        Vendedor vendedor = new Vendedor();
        vendedor.setUsuarioId(2L);
        vendedor.setNombreLegal("Tienda Nueva S.A.");
        vendedor.setDisplayName("Tienda Nueva");
        vendedor.setSlug("tienda-nueva-2");
        vendedor.setStatus(Vendedor.EstatusVendedor.ACTIVO);
        Vendedor guardado = repository.save(vendedor);
        
        // Buscar por ID
        var resultado = repository.findById(guardado.getId());
        
        // Verificar que Optional.isPresent() es true
        assertTrue(resultado.isPresent(), "Debe encontrar el vendedor por ID");
        
        // Verificar datos coinciden
        Vendedor encontrado = resultado.get();
        assertEquals(2L, encontrado.getUsuarioId());
        assertEquals("Tienda Nueva S.A.", encontrado.getNombreLegal());
        assertEquals("Tienda Nueva", encontrado.getDisplayName());
        assertEquals(Vendedor.EstatusVendedor.ACTIVO, encontrado.getStatus());
    }
    
    @Test
    void findAll_DeberiaRetornarLista_CuandoExistenVendedores() {
        // Guardar 3 entidades diferentes
        Vendedor vendedor1 = new Vendedor();
        vendedor1.setUsuarioId(10L);
        vendedor1.setNombreLegal("Primera Tienda S.A.");
        vendedor1.setDisplayName("Primera Tienda");
        vendedor1.setSlug("primera-ti-10");
        vendedor1.setStatus(Vendedor.EstatusVendedor.ACTIVO);
        repository.save(vendedor1);

        Vendedor vendedor2 = new Vendedor();
        vendedor2.setUsuarioId(20L);
        vendedor2.setNombreLegal("Segunda Tienda S.A.");
        vendedor2.setDisplayName("Segunda Tienda");
        vendedor2.setSlug("segunda-ti-20");
        vendedor2.setStatus(Vendedor.EstatusVendedor.ACTIVO);
        repository.save(vendedor2);

        Vendedor vendedor3 = new Vendedor();
        vendedor3.setUsuarioId(30L);
        vendedor3.setNombreLegal("Tercera Tienda S.A.");
        vendedor3.setDisplayName("Tercera Tienda");
        vendedor3.setSlug("tercera-ti-30");
        vendedor3.setStatus(Vendedor.EstatusVendedor.PENDIENTE);
        repository.save(vendedor3);
        
        // Llamar findAll()
        var lista = repository.findAll();
        
        // Verificar que retorna 3+ elementos (puede haber datos previos)
        assertTrue(lista.size() >= 3, "Debe retornar al menos 3 vendedores");
    }
    
    @Test
    void delete_DeberiaEliminarVendedor_CuandoExiste() {
        // Guardar entidad
        Vendedor vendedor = new Vendedor();
        vendedor.setUsuarioId(99L);
        vendedor.setNombreLegal("ParaBorrar S.A.");
        vendedor.setDisplayName("ParaBorrar");
        vendedor.setSlug("para-borrar-99");
        vendedor.setStatus(Vendedor.EstatusVendedor.ACTIVO);
        Vendedor guardado = repository.save(vendedor);
        Long id = guardado.getId();
        
        // Llamar deleteById
        repository.deleteById(id);
        
        // Verificar que repository.findById(id).isEmpty() es true
        assertTrue(repository.findById(id).isEmpty(), "El vendedor debe haber sido eliminado");
    }
    
    @Test
    void update_DeberiaActualizarVendedor_CuandoExiste() {
        // Guardar entidad con nombre "Original"
        Vendedor vendedor = new Vendedor();
        vendedor.setUsuarioId(100L);
        vendedor.setNombreLegal("Original S.A.");
        vendedor.setDisplayName("Original");
        vendedor.setSlug("original-100");
        vendedor.setStatus(Vendedor.EstatusVendedor.PENDIENTE);
        Vendedor guardado = repository.save(vendedor);
        
        // Modificar nombre a "Actualizado"
        guardado.setNombreLegal("Actualizado S.A.");
        guardado.setDisplayName("Actualizado");
        guardado.setStatus(Vendedor.EstatusVendedor.ACTIVO);
        
        // Llamar repository.save(entidadModificada)
        repository.save(guardado);
        
        // Buscar por ID y verificar nombre es "Actualizado"
        var resultado = repository.findById(guardado.getId());
        assertTrue(resultado.isPresent());
        assertEquals("Actualizado S.A.", resultado.get().getNombreLegal());
        assertEquals("Actualizado", resultado.get().getDisplayName());
        assertEquals(Vendedor.EstatusVendedor.ACTIVO, resultado.get().getStatus());
    }
    
    // === TESTS DE API (Controller) ===
    // Aquí van los tests de integración a nivel HTTP usando `restTemplate`.
    // Ejemplos pendientes: POST 201, GET {id} 200, GET lista 200, PUT 200, DELETE 204, POST 400.
    // Mantener separados de los tests de Repository para facilitar pruebas unitarias de controllers luego.
    // @Test void post_DeberiaCrearVendedor_Status201() { /* pendiente */ }
    //REVIEW: implementar controller para Vendedor.
    /*
    @Test
    void post_DeberiaCrearVendedor_Status201() throws Exception {
        Vendedor payload = new Vendedor(); payload.setUserId(1L); payload.setNombre("Vend API"); payload.setDescripcion("d"); payload.setRating(4.0); payload.setActivo(true);
        var result = restTemplate.perform(MockMvcRequestBuilders.post("/api/vendedores")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload))).andExpect(MockMvcResultMatchers.status().isCreated()).andReturn();
        Vendedor resp = objectMapper.readValue(result.getResponse().getContentAsString(), Vendedor.class);
        assertNotNull(resp.getId());
    }

    @Test
    void getById_DeberiaRetornarVendedor_Status200() throws Exception {
        Vendedor v = new Vendedor(); v.setUserId(2L); v.setNombre("G1"); v.setDescripcion("d"); v.setRating(3.0); v.setActivo(true); Vendedor g = repository.save(v);
        var result = restTemplate.perform(MockMvcRequestBuilders.get("/api/vendedores/{id}", g.getId()).accept(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        Vendedor resp = objectMapper.readValue(result.getResponse().getContentAsString(), Vendedor.class);
        assertEquals(g.getId(), resp.getId());
    }

    @Test
    void getList_DeberiaRetornarLista_Status200() throws Exception {
        Vendedor v1 = new Vendedor(); v1.setUserId(10L); v1.setNombre("L1"); v1.setDescripcion("d"); v1.setRating(1.0); v1.setActivo(true); repository.save(v1);
        Vendedor v2 = new Vendedor(); v2.setUserId(20L); v2.setNombre("L2"); v2.setDescripcion("d"); v2.setRating(2.0); v2.setActivo(true); repository.save(v2);
        var result = restTemplate.perform(MockMvcRequestBuilders.get("/api/vendedores").accept(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        Vendedor[] arr = objectMapper.readValue(result.getResponse().getContentAsString(), Vendedor[].class);
        assertTrue(arr.length >= 2);
    }

    @Test
    void put_DeberiaActualizarVendedor_Status200() throws Exception {
        Vendedor v = new Vendedor(); v.setUserId(33L); v.setNombre("Orig"); v.setDescripcion("d"); v.setRating(1.0); v.setActivo(true); Vendedor g = repository.save(v);
        Vendedor upd = new Vendedor(); upd.setId(g.getId()); upd.setUserId(g.getUserId()); upd.setNombre("Upd"); upd.setDescripcion("ud"); upd.setRating(5.0); upd.setActivo(false);
        restTemplate.perform(MockMvcRequestBuilders.put("/api/vendedores/{id}", g.getId()).contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(upd))).andExpect(MockMvcResultMatchers.status().isOk());
        var desdeDb = repository.findById(g.getId()).orElseThrow(); assertEquals("Upd", desdeDb.getNombre());
    }

    @Test
    void delete_DeberiaEliminarVendedor_Status204() throws Exception {
        Vendedor v = new Vendedor(); v.setUserId(99L); v.setNombre("ToDel"); v.setDescripcion("d"); v.setRating(2.0); v.setActivo(true); Vendedor g = repository.save(v);
        restTemplate.perform(MockMvcRequestBuilders.delete("/api/vendedores/{id}", g.getId())).andExpect(MockMvcResultMatchers.status().isNoContent());
        entityManager.flush(); entityManager.clear(); var maybe = repository.findById(g.getId()); assertTrue(maybe.isPresent()); assertNotNull(maybe.get().getDeletedAt());
    }

    @Test
    void post_DeberiaRetornar400_CuandoDatosInvalidos() throws Exception {
        Vendedor payload = new Vendedor(); payload.setUserId(null); payload.setNombre("");
        restTemplate.perform(MockMvcRequestBuilders.post("/api/vendedores").contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload))).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
    */
}
