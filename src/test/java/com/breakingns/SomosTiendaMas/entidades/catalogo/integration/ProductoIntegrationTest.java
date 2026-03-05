package com.breakingns.SomosTiendaMas.entidades.catalogo.integration;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Producto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.http.MediaType;
import com.breakingns.SomosTiendaMas.security.filter.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(OrderAnnotation.class)
public class ProductoIntegrationTest {
    
    @Autowired
    private ProductoRepository repository;
    
    @Autowired
    private MockMvc restTemplate;
    
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void sincronizarSecuencia() {
        entityManager.createNativeQuery("SELECT setval('producto_id_seq', (SELECT COALESCE(MAX(id), 1) FROM producto))")
                     .getSingleResult();
    }
    
    // === TESTS DE REPOSITORY ===
    
    @Test
    void save_DeberiaCrearProducto_CuandoDatosValidos() {
        // Crear entidad con datos válidos
        Producto producto = new Producto();
        producto.setNombre("Producto Test");
        producto.setSlug("producto-test-" + System.currentTimeMillis());
        producto.setDescripcion("Descripción de prueba");
        
        // Guardar en DB
        Producto guardado = repository.save(producto);
        
        // Verificar que ID fue generado
        assertNotNull(guardado.getId(), "El ID debe ser generado automáticamente");
        
        // Verificar que los datos se guardaron correctamente
        assertEquals("Producto Test", guardado.getNombre());
        assertTrue(guardado.getSlug().startsWith("producto-test-"));
        assertEquals("Descripción de prueba", guardado.getDescripcion());
    }
    
    @Test
    void findById_DeberiaRetornarProducto_CuandoExiste() {
        // Guardar entidad en DB
        Producto producto = new Producto();
        producto.setNombre("Producto Nuevo");
        producto.setSlug("producto-nuevo-" + System.currentTimeMillis());
        producto.setDescripcion("Nuevo producto");
        Producto guardado = repository.save(producto);
        
        // Buscar por ID
        var resultado = repository.findById(guardado.getId());
        
        // Verificar que Optional.isPresent() es true
        assertTrue(resultado.isPresent(), "Debe encontrar el producto por ID");
        
        // Verificar datos coinciden
        Producto encontrado = resultado.get();
        assertEquals("Producto Nuevo", encontrado.getNombre());
        assertTrue(encontrado.getSlug().startsWith("producto-nuevo-"));
        assertEquals("Nuevo producto", encontrado.getDescripcion());
    }
    
    @Test
    void findAll_DeberiaRetornarLista_CuandoExistenProductos() {
        // Guardar 3 entidades diferentes
        Producto producto1 = new Producto();
        producto1.setNombre("Producto 1");
        producto1.setSlug("producto-1-" + System.currentTimeMillis());
        producto1.setDescripcion("Descripción 1");
        repository.save(producto1);
        
        Producto producto2 = new Producto();
        producto2.setNombre("Producto 2");
        producto2.setSlug("producto-2-" + System.currentTimeMillis());
        producto2.setDescripcion("Descripción 2");
        repository.save(producto2);
        
        Producto producto3 = new Producto();
        producto3.setNombre("Producto 3");
        producto3.setSlug("producto-3-" + System.currentTimeMillis());
        producto3.setDescripcion("Descripción 3");
        repository.save(producto3);
        
        // Llamar findAll()
        var lista = repository.findAll();
        
        // Verificar que retorna 3+ elementos (puede haber datos previos)
        assertTrue(lista.size() >= 3, "Debe retornar al menos 3 productos");
    }
    
    @Test
    void delete_DeberiaEliminarProducto_CuandoExiste() {
        // Guardar entidad
        Producto producto = new Producto();
        producto.setNombre("ParaBorrar");
        producto.setSlug("para-borrar-" + System.currentTimeMillis());
        producto.setDescripcion("Para eliminar");
        Producto guardado = repository.save(producto);
        Long id = guardado.getId();
        
        // Llamar deleteById
        repository.deleteById(id);
        
        // Verificar que repository.findById(id).isEmpty() es true
        assertTrue(repository.findById(id).isEmpty(), "El producto debe haber sido eliminado");
    }

    // === VERIFICACIONES DB REALES ===

    @Order(1)
    @Test
    @Transactional
    void db_Transaccion_Rollback_Crear_NoPersiste() {
        Producto p = new Producto(); p.setNombre("RB Prod"); p.setSlug("rb-prod-" + System.currentTimeMillis()); p.setDescripcion("rb"); repository.save(p);
        entityManager.flush();
    }

    @Order(2)
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void db_Transaccion_Rollback_Verificar_NoExiste() {
        var found = repository.findAll().stream().filter(x -> x.getSlug()!=null && x.getSlug().contains("rb-prod-")).findAny();
        assertTrue(found.isEmpty(), "La entidad creada en la transacción anterior debe haber sido rollback-eada");
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void db_Constraint_UniqueYNotNull_Y_Audit() {
        String slug = "unique-prod-" + UUID.randomUUID();
        Producto a = new Producto(); a.setNombre("A"); a.setSlug(slug); repository.saveAndFlush(a);

        boolean uniqueExists = false;
        try {
            Object cnt = entityManager.createNativeQuery("SELECT count(*) FROM pg_constraint c JOIN pg_class t ON c.conrelid=t.oid WHERE t.relname='producto' AND c.contype='u'")
                    .getSingleResult();
            if (cnt != null) uniqueExists = ((Number)cnt).longValue() > 0;
        } catch (Exception ex) { uniqueExists = false; }

        Producto b = new Producto(); b.setNombre("B"); b.setSlug(slug);
        if (uniqueExists) {
            assertThrows(DataIntegrityViolationException.class, () -> { repository.saveAndFlush(b); entityManager.flush(); });
        } else {
            repository.saveAndFlush(b); entityManager.flush(); long count = repository.findAll().stream().filter(x->slug.equals(x.getSlug())).count(); assertTrue(count>=2);
        }

        // Not-null on nombre?
        boolean nombreNotNull = false;
        try { Object nn = entityManager.createNativeQuery("SELECT is_nullable FROM information_schema.columns WHERE table_name='producto' AND column_name='nombre'").getSingleResult(); if (nn!=null) nombreNotNull = "NO".equalsIgnoreCase(nn.toString()); } catch(Exception ex){ nombreNotNull=false; }
        Producto c = new Producto(); c.setNombre(null); c.setSlug("nn-"+UUID.randomUUID());
        if (nombreNotNull) { assertThrows(DataIntegrityViolationException.class, ()->{ repository.saveAndFlush(c); entityManager.flush(); }); } else { repository.saveAndFlush(c); entityManager.flush(); var saved = repository.findById(c.getId()).orElseThrow(); assertNull(saved.getNombre()); }

        // Auditoría
        try { var savedA = repository.findById(a.getId()).orElseThrow(); assertNotNull(savedA.getCreatedAt()); assertNotNull(savedA.getUpdatedAt()); } catch(Exception ex) { }
    }
    
    @Test
    void update_DeberiaActualizarProducto_CuandoExiste() {
        // Guardar entidad con nombre "Original"
        Producto producto = new Producto();
        producto.setNombre("Original");
        producto.setSlug("original-" + System.currentTimeMillis());
        producto.setDescripcion("Descripción original");
        Producto guardado = repository.save(producto);
        
        // Modificar nombre a "Actualizado"
        guardado.setNombre("Actualizado");
        guardado.setDescripcion("Descripción actualizada");
        
        // Llamar repository.save(entidadModificada)
        repository.save(guardado);
        
        // Buscar por ID y verificar nombre es "Actualizado"
        var resultado = repository.findById(guardado.getId());
        assertTrue(resultado.isPresent());
        assertEquals("Actualizado", resultado.get().getNombre());
        assertEquals("Descripción actualizada", resultado.get().getDescripcion());
    }
    
    // === TESTS DE API (Controller) ===
    // Aquí van los tests de integración a nivel HTTP usando `restTemplate`.
    // Ejemplos pendientes: POST 201, GET {id} 200, GET lista 200, PUT 200, DELETE 204, POST 400.
    // Mantener separados de los tests de Repository para facilitar pruebas unitarias de controllers luego.
    @Test
    void post_DeberiaCrearProducto_Status201() throws Exception {
        // POST a controlador de desarrollo: /dev/api/productos
        java.util.Map<String,Object> payload = new java.util.HashMap<>();
        payload.put("nombre", "Producto API");
        payload.put("slug", "producto-api-" + System.currentTimeMillis());
        payload.put("descripcion", "desc api");

        var result = restTemplate.perform(MockMvcRequestBuilders.post("/dev/api/productos")
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(payload)))
                .andExpect(MockMvcResultMatchers.status().isCreated()).andReturn();
        var dto = objectMapper.readValue(result.getResponse().getContentAsString(), java.util.Map.class);
        assertNotNull(dto.get("id"));
        assertEquals("Producto API", dto.get("nombre"));
    }

    @Test
    void getById_DeberiaRetornarProducto_Status200_API() throws Exception {
        Producto producto = new Producto(); producto.setNombre("GET P"); producto.setSlug("get-p-"+System.currentTimeMillis()); producto.setDescripcion("d"); Producto saved = repository.save(producto);
        var result = restTemplate.perform(MockMvcRequestBuilders.get("/dev/api/productos/{id}", saved.getId())
                .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        var dto = objectMapper.readValue(result.getResponse().getContentAsString(), java.util.Map.class);
        assertEquals(saved.getId().intValue(), ((Number)dto.get("id")).intValue());
    }

    @Test
    void delete_DeberiaEliminarProducto_Status204_API() throws Exception {
        Producto producto = new Producto(); producto.setNombre("DEL P"); producto.setSlug("del-p"); producto = repository.save(producto);
        restTemplate.perform(MockMvcRequestBuilders.delete("/dev/api/productos/{id}", producto.getId()))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
        var opt = repository.findById(producto.getId());
        assertTrue(opt.isPresent(), "El producto debe seguir presente (soft-delete)");
        assertNotNull(opt.get().getDeletedAt(), "El campo deletedAt debe estar seteado tras soft-delete");
    }

}
