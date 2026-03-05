package com.breakingns.SomosTiendaMas.entidades.catalogo.integration;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Etiqueta;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.EtiquetaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.MediaType;

import java.util.Arrays;
import java.util.List;
import jakarta.persistence.EntityManager;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Order;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.breakingns.SomosTiendaMas.security.filter.JwtAuthenticationFilter;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc(addFilters = false)
@TestMethodOrder(OrderAnnotation.class)
@Transactional
public class EtiquetaIntegrationTest {

    private static String SHARED_ROLLBACK_SLUG = "rollback-slug-etiqueta";
    
    @Autowired
    private EtiquetaRepository repository;
    
    @Autowired
    private MockMvc restTemplate; // named restTemplate previously, keep usage minimal

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private EntityManager entityManager;
    
    // === TESTS DE REPOSITORY ===
    
    @Test
    void save_DeberiaCrearEtiqueta_CuandoDatosValidos() {
        // Crear entidad con datos válidos
        Etiqueta etiqueta = new Etiqueta();
        etiqueta.setNombre("Oferta");
        etiqueta.setSlug("oferta-test");
        
        // Guardar en DB
        Etiqueta guardada = repository.save(etiqueta);
        
        // Verificar que ID fue generado
        assertNotNull(guardada.getId(), "El ID debe ser generado automáticamente");
        
        // Verificar que los datos se guardaron correctamente
        assertEquals("Oferta", guardada.getNombre());
        assertEquals("oferta-test", guardada.getSlug());
    }
    
    @Test
    void findById_DeberiaRetornarEtiqueta_CuandoExiste() {
        // Guardar entidad en DB
        Etiqueta etiqueta = new Etiqueta();
        etiqueta.setNombre("Nueva");
        etiqueta.setSlug("nueva-test");
        Etiqueta guardada = repository.save(etiqueta);
        
        // Buscar por ID
        var resultado = repository.findById(guardada.getId());
        
        // Verificar que Optional.isPresent() es true
        assertTrue(resultado.isPresent(), "Debe encontrar la etiqueta por ID");
        
        // Verificar datos coinciden
        Etiqueta encontrada = resultado.get();
        assertEquals("Nueva", encontrada.getNombre());
        assertEquals("nueva-test", encontrada.getSlug());
    }
    
    @Test
    void findAll_DeberiaRetornarLista_CuandoExistenEtiquetas() {
        // Guardar 3 entidades diferentes
        Etiqueta etiqueta1 = new Etiqueta();
        etiqueta1.setNombre("Primera");
        etiqueta1.setSlug("primera-test");
        repository.save(etiqueta1);
        
        Etiqueta etiqueta2 = new Etiqueta();
        etiqueta2.setNombre("Segunda");
        etiqueta2.setSlug("segunda-test");
        repository.save(etiqueta2);
        
        Etiqueta etiqueta3 = new Etiqueta();
        etiqueta3.setNombre("Tercera");
        etiqueta3.setSlug("tercera-test");
        repository.save(etiqueta3);
        
        // Llamar findAll()
        var lista = repository.findAll();
        
        // Verificar que retorna 3+ elementos (puede haber datos previos)
        assertTrue(lista.size() >= 3, "Debe retornar al menos 3 etiquetas");
    }
    
    @Test
    void delete_DeberiaEliminarEtiqueta_CuandoExiste() {
        // Guardar entidad
        Etiqueta etiqueta = new Etiqueta();
        etiqueta.setNombre("ParaBorrar");
        etiqueta.setSlug("para-borrar-test");
        Etiqueta guardada = repository.save(etiqueta);
        Long id = guardada.getId();
        
        // Llamar deleteById
        repository.deleteById(id);
        
        // Verificar que repository.findById(id).isEmpty() es true
        assertTrue(repository.findById(id).isEmpty(), "La etiqueta debe haber sido eliminada");
    }
    
    @Test
    void update_DeberiaActualizarEtiqueta_CuandoExiste() {
        // Guardar entidad con nombre "Original"
        Etiqueta etiqueta = new Etiqueta();
        etiqueta.setNombre("Original");
        etiqueta.setSlug("original-test");
        Etiqueta guardada = repository.save(etiqueta);
        
        // Modificar nombre a "Actualizado"
        guardada.setNombre("Actualizado");
        guardada.setSlug("actualizado-test");
        
        // Llamar repository.save(entidadModificada)
        repository.save(guardada);
        
        // Buscar por ID y verificar nombre es "Actualizado"
        var resultado = repository.findById(guardada.getId());
        assertTrue(resultado.isPresent());
        assertEquals("Actualizado", resultado.get().getNombre());
        assertEquals("actualizado-test", resultado.get().getSlug());
    }
    
    // === TESTS DE API (Controller) ===
    // Tests de integración a nivel HTTP usando `restTemplate`.

    @Test
    void post_DeberiaCrearEtiqueta_Status201() throws Exception {
        Etiqueta payload = new Etiqueta();
        payload.setNombre("Etiqueta API");
        payload.setSlug("etiqueta-api-" + System.currentTimeMillis());

        var result = restTemplate.perform(MockMvcRequestBuilders
                .post("/api/etiquetas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        Etiqueta resp = objectMapper.readValue(body, Etiqueta.class);

        assertNotNull(resp);
        assertNotNull(resp.getId(), "La respuesta debe incluir el ID generado");
        assertEquals(payload.getNombre(), resp.getNombre());
    }

    @Test
    void getById_DeberiaRetornarEtiqueta_Status200() throws Exception {
        Etiqueta etiqueta = new Etiqueta();
        etiqueta.setNombre("Etiqueta GET");
        etiqueta.setSlug("etiqueta-get-" + System.currentTimeMillis());
        Etiqueta guardada = repository.save(etiqueta);

        var result = restTemplate.perform(MockMvcRequestBuilders
                .get("/api/etiquetas/{id}", guardada.getId())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        Etiqueta resp = objectMapper.readValue(body, Etiqueta.class);
        assertEquals(guardada.getId(), resp.getId());
        assertEquals(guardada.getNombre(), resp.getNombre());
    }

    @Test
    void getList_DeberiaRetornarLista_Status200() throws Exception {
        Etiqueta e1 = new Etiqueta(); e1.setNombre("Lista1"); e1.setSlug("lista1-"+System.currentTimeMillis()); repository.save(e1);
        Etiqueta e2 = new Etiqueta(); e2.setNombre("Lista2"); e2.setSlug("lista2-"+System.currentTimeMillis()); repository.save(e2);

        var result = restTemplate.perform(MockMvcRequestBuilders
                .get("/api/etiquetas")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        Etiqueta[] arr = objectMapper.readValue(body, Etiqueta[].class);
        List<Etiqueta> lista = Arrays.asList(arr);
        assertTrue(lista.size() >= 2, "La lista debe contener al menos las etiquetas creadas");
    }

    @Test
    void put_DeberiaActualizarEtiqueta_Status200() throws Exception {
        Etiqueta etiqueta = new Etiqueta();
        etiqueta.setNombre("Original PUT");
        etiqueta.setSlug("original-put-" + System.currentTimeMillis());
        Etiqueta guardada = repository.save(etiqueta);

        Etiqueta actualizado = new Etiqueta();
        actualizado.setId(guardada.getId());
        actualizado.setNombre("Actualizado PUT");
        actualizado.setSlug("actualizado-put-" + System.currentTimeMillis());

        var result = restTemplate.perform(MockMvcRequestBuilders
                .put("/api/etiquetas/{id}", guardada.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(actualizado)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        Etiqueta desdeDb = repository.findById(guardada.getId()).orElseThrow();
        assertEquals("Actualizado PUT", desdeDb.getNombre());
    }

    @Test
    void delete_DeberiaEliminarEtiqueta_Status204() throws Exception {
        Etiqueta etiqueta = new Etiqueta();
        etiqueta.setNombre("Para Eliminar");
        etiqueta.setSlug("para-eliminar-" + System.currentTimeMillis());
        Etiqueta guardada = repository.save(etiqueta);

        restTemplate.perform(MockMvcRequestBuilders
            .delete("/api/etiquetas/{id}", guardada.getId()))
            .andExpect(MockMvcResultMatchers.status().isNoContent());

        // Forzar lectura desde la BD (evitar cached persistence context)
        entityManager.flush();
        entityManager.clear();

        var maybe = repository.findById(guardada.getId());
        assertTrue(maybe.isPresent(), "La entidad debe seguir existiendo en la tabla (soft-delete)");
        assertNotNull(maybe.get().getDeletedAt(), "Debe haberse marcado deletedAt tras el soft-delete");
    }

    @Test
    void post_DeberiaRetornar400_CuandoDatosInvalidos() throws Exception {
        Etiqueta payload = new Etiqueta();
        payload.setNombre("");
        payload.setSlug("");

        restTemplate.perform(MockMvcRequestBuilders
                .post("/api/etiquetas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    // === VERIFICACIONES DB REALES ===
    
    @Test
    @Order(90)
    void tx_InsertaYSeRollbackea_Etiqueta() {
        String slug = SHARED_ROLLBACK_SLUG + System.currentTimeMillis();
        Etiqueta e = new Etiqueta(); e.setNombre("Temp"); e.setSlug(slug);
        repository.save(e);
        assertTrue(repository.findBySlug(slug).isPresent());
        SHARED_ROLLBACK_SLUG = slug;
    }

    @Test
    @Order(91)
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void verify_NoPersistioDespuesDeRollback_Etiqueta() {
        assertTrue(repository.findBySlug(SHARED_ROLLBACK_SLUG).isEmpty());
    }

    @Test
    void unique_Slug_DeberiaLanzarExcepcion_Etiqueta() {
        String slug = "dup-slug-etiq-" + System.currentTimeMillis();
        Etiqueta a = new Etiqueta(); a.setNombre("A"); a.setSlug(slug);
        Etiqueta b = new Etiqueta(); b.setNombre("B"); b.setSlug(slug);
        repository.saveAndFlush(a);
        assertThrows(DataIntegrityViolationException.class, () -> repository.saveAndFlush(b));
    }

    @Test
    void notNull_Nombre_DeberiaLanzarExcepcion_Etiqueta() {
        Etiqueta e = new Etiqueta(); e.setSlug("no-name-" + System.currentTimeMillis());
        assertThrows(DataIntegrityViolationException.class, () -> repository.saveAndFlush(e));
    }

    @Test
    void auditoria_CreatedAndUpdated_ShouldWork_Etiqueta() throws InterruptedException {
        Etiqueta e = new Etiqueta(); e.setNombre("Audit"); e.setSlug("audit-" + System.currentTimeMillis());
        Etiqueta saved = repository.saveAndFlush(e);
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
        var created = saved.getCreatedAt();
        Thread.sleep(50);
        saved.setNombre("Audit-Updated");
        Etiqueta updated = repository.saveAndFlush(saved);
        assertTrue(updated.getUpdatedAt().isAfter(created));
    }

}
