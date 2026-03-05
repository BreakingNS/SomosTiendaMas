package com.breakingns.SomosTiendaMas.entidades.catalogo.integration;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Marca;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.MarcaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.breakingns.SomosTiendaMas.security.filter.JwtAuthenticationFilter;
import jakarta.persistence.EntityManager;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Order;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@TestMethodOrder(OrderAnnotation.class)
@Transactional
public class MarcaIntegrationTest {

    private static String SHARED_ROLLBACK_SLUG = "rollback-slug-marca";
    
    @Autowired
    private MarcaRepository repository;

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
    void save_DeberiaCrearMarca_CuandoDatosValidos() {
        // Crear entidad con datos válidos
        Marca marca = new Marca();
        marca.setNombre("Nike");
        marca.setSlug("nike-test");
        marca.setDescripcion("Marca deportiva");
        
        // Guardar en DB
        Marca guardada = repository.save(marca);
        
        // Verificar que ID fue generado
        assertNotNull(guardada.getId(), "El ID debe ser generado automáticamente");
        
        // Verificar que los datos se guardaron correctamente
        assertEquals("Nike", guardada.getNombre());
        assertEquals("nike-test", guardada.getSlug());
        assertEquals("Marca deportiva", guardada.getDescripcion());
    }
    
    @Test
    void findById_DeberiaRetornarMarca_CuandoExiste() {
        // Guardar entidad en DB
        Marca marca = new Marca();
        marca.setNombre("Adidas");
        marca.setSlug("adidas-test");
        marca.setDescripcion("Marca de ropa");
        Marca guardada = repository.save(marca);
        
        // Buscar por ID
        var resultado = repository.findById(guardada.getId());
        
        // Verificar que Optional.isPresent() es true
        assertTrue(resultado.isPresent(), "Debe encontrar la marca por ID");
        
        // Verificar datos coinciden
        Marca encontrada = resultado.get();
        assertEquals("Adidas", encontrada.getNombre());
        assertEquals("adidas-test", encontrada.getSlug());
        assertEquals("Marca de ropa", encontrada.getDescripcion());
    }
    
    @Test
    void findAll_DeberiaRetornarLista_CuandoExistenMarcas() {
        // Guardar 3 entidades diferentes
        Marca marca1 = new Marca();
        marca1.setNombre("Primera");
        marca1.setSlug("primera-test"); 
        marca1.setDescripcion("Primera marca");
        repository.save(marca1);
        
        Marca marca2 = new Marca();
        marca2.setNombre("Segunda");
        marca2.setSlug("segunda-test");
        marca2.setDescripcion("Segunda marca");
        repository.save(marca2);
        
        Marca marca3 = new Marca();
        marca3.setNombre("Tercera");
        marca3.setSlug("tercera-test");
        marca3.setDescripcion("Tercera marca");
        repository.save(marca3);
        
        // Llamar findAll()
        var lista = repository.findAll();
        
        // Verificar que retorna 3+ elementos (puede haber datos previos)
        assertTrue(lista.size() >= 3, "Debe retornar al menos 3 marcas");
    }
    
    @Test
    void delete_DeberiaEliminarMarca_CuandoExiste() {
        // Guardar entidad
        Marca marca = new Marca();
        marca.setNombre("ParaBorrar");
        marca.setSlug("para-borrar-test");
        marca.setDescripcion("Para eliminar");
        Marca guardada = repository.save(marca);
        Long id = guardada.getId();
        
        // Llamar deleteById
        repository.deleteById(id);
        
        // Verificar que repository.findById(id).isEmpty() es true
        assertTrue(repository.findById(id).isEmpty(), "La marca debe haber sido eliminada");
    }
    
    @Test
    void update_DeberiaActualizarMarca_CuandoExiste() {
        // Guardar entidad con nombre "Original"
        Marca marca = new Marca();
        marca.setNombre("Original");
        marca.setSlug("original-test");
        marca.setDescripcion("Descripción original");
        Marca guardada = repository.save(marca);
        
        // Modificar nombre a "Actualizado"
        guardada.setNombre("Actualizado");
        guardada.setSlug("actualizado-test");
        guardada.setDescripcion("Descripción actualizada");
        
        // Llamar repository.save(entidadModificada)
        repository.save(guardada);
        
        // Buscar por ID y verificar nombre es "Actualizado"
        var resultado = repository.findById(guardada.getId());
        assertTrue(resultado.isPresent());
        assertEquals("Actualizado", resultado.get().getNombre());
        assertEquals("actualizado-test", resultado.get().getSlug());
        assertEquals("Descripción actualizada", resultado.get().getDescripcion());
    }
    
    // === TESTS DE API (Controller) ===
    // Aquí van los tests de integración a nivel HTTP usando `restTemplate`.
    // Ejemplos pendientes: POST 201, GET {id} 200, GET lista 200, PUT 200, DELETE 204, POST 400.
    // Mantener separados de los tests de Repository para facilitar pruebas unitarias de controllers luego.
    
    @Test
    void post_DeberiaCrearMarca_Status201() throws Exception {
        Marca payload = new Marca();
        payload.setNombre("Marca API");
        payload.setSlug("marca-api-" + System.currentTimeMillis());
        payload.setDescripcion("Desc API");

        var result = restTemplate.perform(MockMvcRequestBuilders
                .post("/api/marcas")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        Marca resp = objectMapper.readValue(body, Marca.class);
        assertNotNull(resp);
        assertNotNull(resp.getId());
        assertEquals(payload.getNombre(), resp.getNombre());
    }

    @Test
    void getById_DeberiaRetornarMarca_Status200() throws Exception {
        Marca marca = new Marca(); marca.setNombre("GET Marca"); marca.setSlug("get-marca-"+System.currentTimeMillis()); marca.setDescripcion("d");
        Marca guardada = repository.save(marca);

        var result = restTemplate.perform(MockMvcRequestBuilders
                .get("/api/marcas/{id}", guardada.getId()).accept(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        Marca resp = objectMapper.readValue(result.getResponse().getContentAsString(), Marca.class);
        assertEquals(guardada.getId(), resp.getId());
    }

    @Test
    void getList_DeberiaRetornarLista_Status200() throws Exception {
        Marca m1 = new Marca(); m1.setNombre("L1"); m1.setSlug("l1-"+System.currentTimeMillis()); m1.setDescripcion("d1"); repository.save(m1);
        Marca m2 = new Marca(); m2.setNombre("L2"); m2.setSlug("l2-"+System.currentTimeMillis()); m2.setDescripcion("d2"); repository.save(m2);

        var result = restTemplate.perform(MockMvcRequestBuilders.get("/api/marcas").accept(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

        Marca[] arr = objectMapper.readValue(result.getResponse().getContentAsString(), Marca[].class);
        assertTrue(arr.length >= 2);
    }

    @Test
    void put_DeberiaActualizarMarca_Status200() throws Exception {
        Marca marca = new Marca(); marca.setNombre("Orig"); marca.setSlug("orig-"+System.currentTimeMillis()); marca.setDescripcion("d"); Marca guardada = repository.save(marca);
        Marca actualizado = new Marca(); actualizado.setId(guardada.getId()); actualizado.setNombre("Upd"); actualizado.setSlug("upd"); actualizado.setDescripcion("upd");

        restTemplate.perform(MockMvcRequestBuilders.put("/api/marcas/{id}", guardada.getId())
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(actualizado))).andExpect(MockMvcResultMatchers.status().isOk());

        Marca desdeDb = repository.findById(guardada.getId()).orElseThrow();
        assertEquals("Upd", desdeDb.getNombre());
    }

    @Test
    void delete_DeberiaEliminarMarca_Status204() throws Exception {
        Marca marca = new Marca(); marca.setNombre("ToDel"); marca.setSlug("todel-"+System.currentTimeMillis()); marca.setDescripcion("d"); Marca guardada = repository.save(marca);

        restTemplate.perform(MockMvcRequestBuilders.delete("/api/marcas/{id}", guardada.getId()))
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        entityManager.flush(); entityManager.clear();
        var maybe = repository.findById(guardada.getId());
        assertTrue(maybe.isPresent());
        assertNotNull(maybe.get().getDeletedAt());
    }

    @Test
    void post_DeberiaRetornar400_CuandoDatosInvalidos() throws Exception {
        Marca payload = new Marca(); payload.setNombre(""); payload.setSlug(""); payload.setDescripcion("");
        restTemplate.perform(MockMvcRequestBuilders.post("/api/marcas").contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload))).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    // === VERIFICACIONES DB REALES ===

    @Test
    @Order(90)
    void tx_InsertaYSeRollbackea_Marca() {
        String slug = SHARED_ROLLBACK_SLUG + System.currentTimeMillis();
        Marca m = new Marca(); m.setNombre("Temp"); m.setSlug(slug); m.setDescripcion("d");
        repository.save(m);
        assertTrue(repository.findBySlug(slug).isPresent());
        SHARED_ROLLBACK_SLUG = slug;
    }

    @Test
    @Order(91)
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void verify_NoPersistioDespuesDeRollback_Marca() {
        assertTrue(repository.findBySlug(SHARED_ROLLBACK_SLUG).isEmpty());
    }

    @Test
    void unique_Slug_DeberiaLanzarExcepcion_Marca() {
        String slug = "dup-slug-marca-" + System.currentTimeMillis();
        Marca a = new Marca(); a.setNombre("A"); a.setSlug(slug); a.setDescripcion("d");
        Marca b = new Marca(); b.setNombre("B"); b.setSlug(slug); b.setDescripcion("d");
        repository.saveAndFlush(a);
        assertThrows(DataIntegrityViolationException.class, () -> repository.saveAndFlush(b));
    }

    @Test
    void notNull_Nombre_DeberiaLanzarExcepcion_Marca() {
        Marca m = new Marca(); m.setSlug("no-name-marca-" + System.currentTimeMillis()); m.setDescripcion("d");
        assertThrows(DataIntegrityViolationException.class, () -> repository.saveAndFlush(m));
    }

    @Test
    void auditoria_CreatedAndUpdated_ShouldWork_Marca() throws InterruptedException {
        Marca m = new Marca(); m.setNombre("Audit"); m.setSlug("audit-marca-" + System.currentTimeMillis()); m.setDescripcion("d");
        Marca saved = repository.saveAndFlush(m);
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
        var created = saved.getCreatedAt();
        Thread.sleep(50);
        saved.setNombre("Audit-Updated");
        Marca updated = repository.saveAndFlush(saved);
        assertTrue(updated.getUpdatedAt().isAfter(created));
    }

}
