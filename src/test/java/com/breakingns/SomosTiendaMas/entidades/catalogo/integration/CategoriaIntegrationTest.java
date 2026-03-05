package com.breakingns.SomosTiendaMas.entidades.catalogo.integration;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Categoria;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.CategoriaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.breakingns.SomosTiendaMas.security.filter.JwtAuthenticationFilter;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Order;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    properties = "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration,org.springframework.boot.autoconfigure.http.client.HttpClientAutoConfiguration"
)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@TestMethodOrder(OrderAnnotation.class)
@Transactional
public class CategoriaIntegrationTest {

    private static String SHARED_ROLLBACK_SLUG = "rollback-slug-categoria";
    
    @Autowired
    private CategoriaRepository repository;

    @Autowired
    private MockMvc restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @BeforeEach
    void sincronizarSecuencia() {
        entityManager.createNativeQuery("SELECT setval('categoria_id_seq', (SELECT COALESCE(MAX(id), 1) FROM categoria))")
                     .getSingleResult();
    }
    
    // === TESTS DE REPOSITORY ===
    
    @Test
    void save_DeberiaCrearCategoria_CuandoDatosValidos() {
        // Crear entidad con datos válidos
        Categoria categoria = new Categoria();
        categoria.setNombre("ejemplo");
        categoria.setSlug("ejemplo-test");
        categoria.setDescripcion("Descripción de ejemplo");
        
        // Guardar en DB
        Categoria guardada = repository.save(categoria);
        
        // Verificar que ID fue generado
        assertNotNull(guardada.getId(), "El ID debe ser generado automáticamente");
        
        // Verificar que los datos se guardaron correctamente
        assertEquals("ejemplo", guardada.getNombre());
        assertEquals("ejemplo-test", guardada.getSlug());
        assertEquals("Descripción de ejemplo", guardada.getDescripcion());
    }
    
    @Test
    void findById_DeberiaRetornarCategoria_CuandoExiste() {
        // Guardar entidad en DB
        Categoria categoria = new Categoria();
        categoria.setNombre("Ropa");
        categoria.setSlug("ropa-test");
        categoria.setDescripcion("Prendas de vestir");
        Categoria guardada = repository.save(categoria);
        
        // Buscar por ID
        var resultado = repository.findById(guardada.getId());
        
        // Verificar que Optional.isPresent() es true
        assertTrue(resultado.isPresent(), "Debe encontrar la categoría por ID");
        
        // Verificar datos coinciden
        Categoria encontrada = resultado.get();
        assertEquals("Ropa", encontrada.getNombre());
        assertEquals("ropa-test", encontrada.getSlug());
        assertEquals("Prendas de vestir", encontrada.getDescripcion());
    }
    
    @Test
    void findAll_DeberiaRetornarLista_CuandoExistenCategorias() {
        // Guardar 3 entidades diferentes
        Categoria categoria1 = new Categoria();
        categoria1.setNombre("Primera");
        categoria1.setSlug("primera-test");
        categoria1.setDescripcion("Primera categoría");
        repository.save(categoria1);
        
        Categoria categoria2 = new Categoria();
        categoria2.setNombre("Segunda");
        categoria2.setSlug("segunda-test");
        categoria2.setDescripcion("Segunda categoría");
        repository.save(categoria2);
        
        Categoria categoria3 = new Categoria();
        categoria3.setNombre("Tercera");
        categoria3.setSlug("tercera-test");
        categoria3.setDescripcion("Tercera categoría");
        repository.save(categoria3);
        
        // Llamar findAll()
        var lista = repository.findAll();
        
        // Verificar que retorna 3+ elementos (puede haber datos previos)
        assertTrue(lista.size() >= 3, "Debe retornar al menos 3 categorías");
    }
    
    @Test
    void delete_DeberiaEliminarCategoria_CuandoExiste() {
        // Guardar entidad
        Categoria categoria = new Categoria();
        categoria.setNombre("ParaBorrar");
        categoria.setSlug("para-borrar-test");
        categoria.setDescripcion("Para eliminar");
        Categoria guardada = repository.save(categoria);
        Long id = guardada.getId();
        
        // Llamar deleteById
        repository.deleteById(id);
        
        // Verificar que repository.findById(id).isEmpty() es true
        assertTrue(repository.findById(id).isEmpty(), "La categoría debe haber sido eliminada");
    }
    
    @Test
    void update_DeberiaActualizarCategoria_CuandoExiste() {
        // Guardar entidad con nombre "Original"
        Categoria categoria = new Categoria();
        categoria.setNombre("Original");
        categoria.setSlug("original-test");
        categoria.setDescripcion("Descripción original");
        Categoria guardada = repository.save(categoria);
        
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
    // @Test void post_DeberiaCrearCategoria_Status201() { /* pendiente */ }

    @Test
    void post_DeberiaCrearCategoria_Status201() throws Exception {
        Categoria payload = new Categoria(); payload.setNombre("Cat API"); payload.setSlug("cat-api-"+System.currentTimeMillis()); payload.setDescripcion("d");
        var result = restTemplate.perform(MockMvcRequestBuilders.post("/api/categorias")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload))).andExpect(MockMvcResultMatchers.status().isCreated()).andReturn();
        Categoria resp = objectMapper.readValue(result.getResponse().getContentAsString(), Categoria.class);
        assertNotNull(resp.getId());
    }

    @Test
    void getById_DeberiaRetornarCategoria_Status200() throws Exception {
        Categoria c = new Categoria(); c.setNombre("G1"); c.setSlug("g1-"+System.currentTimeMillis()); c.setDescripcion("d"); Categoria g = repository.save(c);
        var result = restTemplate.perform(MockMvcRequestBuilders.get("/api/categorias/{id}", g.getId()).accept(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        Categoria resp = objectMapper.readValue(result.getResponse().getContentAsString(), Categoria.class);
        assertEquals(g.getId(), resp.getId());
    }

    @Test
    void getList_DeberiaRetornarLista_Status200() throws Exception {
        Categoria a = new Categoria(); a.setNombre("L1"); a.setSlug("l1-"+System.currentTimeMillis()); a.setDescripcion("d"); repository.save(a);
        Categoria b = new Categoria(); b.setNombre("L2"); b.setSlug("l2-"+System.currentTimeMillis()); b.setDescripcion("d"); repository.save(b);
        var result = restTemplate.perform(MockMvcRequestBuilders.get("/api/categorias").accept(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        Categoria[] arr = objectMapper.readValue(result.getResponse().getContentAsString(), Categoria[].class);
        assertTrue(arr.length >= 2);
    }

    @Test
    void put_DeberiaActualizarCategoria_Status200() throws Exception {
        Categoria c = new Categoria(); c.setNombre("Orig"); c.setSlug("orig-"+System.currentTimeMillis()); c.setDescripcion("d"); Categoria g = repository.save(c);
        Categoria upd = new Categoria(); upd.setId(g.getId()); upd.setNombre("Upd"); upd.setSlug("upd"); upd.setDescripcion("ud");
        restTemplate.perform(MockMvcRequestBuilders.put("/api/categorias/{id}", g.getId()).contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(upd))).andExpect(MockMvcResultMatchers.status().isOk());
        var desdeDb = repository.findById(g.getId()).orElseThrow(); assertEquals("Upd", desdeDb.getNombre());
    }

    @Test
    void delete_DeberiaEliminarCategoria_Status204() throws Exception {
        Categoria c = new Categoria(); c.setNombre("ToDel"); c.setSlug("td-"+System.currentTimeMillis()); c.setDescripcion("d"); Categoria g = repository.save(c);
        restTemplate.perform(MockMvcRequestBuilders.delete("/api/categorias/{id}", g.getId())).andExpect(MockMvcResultMatchers.status().isNoContent());
        entityManager.flush(); entityManager.clear(); var maybe = repository.findById(g.getId()); assertTrue(maybe.isPresent()); assertNotNull(maybe.get().getDeletedAt());
    }

    @Test
    void post_DeberiaRetornar400_CuandoDatosInvalidos() throws Exception {
        Categoria payload = new Categoria(); payload.setNombre(""); payload.setSlug(""); restTemplate.perform(MockMvcRequestBuilders.post("/api/categorias").contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload))).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    // === VERIFICACIONES DB REALES ===

    @Test
    @Order(90)
    void tx_InsertaYSeRollbackea_Categoria() {
        String slug = SHARED_ROLLBACK_SLUG + System.currentTimeMillis();
        Categoria c = new Categoria(); c.setNombre("Temp"); c.setSlug(slug); c.setDescripcion("d");
        repository.save(c);
        assertTrue(repository.findBySlug(slug).isPresent());
        SHARED_ROLLBACK_SLUG = slug;
    }

    @Test
    @Order(91)
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void verify_NoPersistioDespuesDeRollback_Categoria() {
        assertTrue(repository.findBySlug(SHARED_ROLLBACK_SLUG).isEmpty());
    }

    @Test
    void unique_Slug_DeberiaLanzarExcepcion_Categoria() {
        String slug = "dup-slug-cat-" + System.currentTimeMillis();
        Categoria a = new Categoria(); a.setNombre("A"); a.setSlug(slug); a.setDescripcion("d");
        Categoria b = new Categoria(); b.setNombre("B"); b.setSlug(slug); b.setDescripcion("d");
        repository.saveAndFlush(a);
        assertThrows(DataIntegrityViolationException.class, () -> repository.saveAndFlush(b));
    }

    @Test
    void notNull_Nombre_DeberiaLanzarExcepcion_Categoria() {
        Categoria c = new Categoria(); c.setSlug("no-name-cat-" + System.currentTimeMillis()); c.setDescripcion("d");
        assertThrows(DataIntegrityViolationException.class, () -> repository.saveAndFlush(c));
    }

    @Test
    void auditoria_CreatedAndUpdated_ShouldWork_Categoria() throws InterruptedException {
        Categoria c = new Categoria(); c.setNombre("Audit"); c.setSlug("audit-cat-" + System.currentTimeMillis()); c.setDescripcion("d");
        Categoria saved = repository.saveAndFlush(c);
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
        var created = saved.getCreatedAt();
        Thread.sleep(50);
        saved.setNombre("Audit-Updated");
        Categoria updated = repository.saveAndFlush(saved);
        assertTrue(updated.getUpdatedAt().isAfter(created));
    }

}
