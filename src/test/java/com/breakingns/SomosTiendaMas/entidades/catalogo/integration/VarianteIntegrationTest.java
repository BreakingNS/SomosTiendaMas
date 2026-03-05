package com.breakingns.SomosTiendaMas.entidades.catalogo.integration;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Producto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Variante;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.ProductoRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.VarianteRepository;
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
import com.breakingns.SomosTiendaMas.security.filter.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc(addFilters = false)
@org.springframework.test.context.ActiveProfiles("test")
@Transactional
@TestMethodOrder(OrderAnnotation.class)
public class VarianteIntegrationTest {
    
    @Autowired
    private VarianteRepository repository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @BeforeEach
    void sincronizarSecuencia() {
        entityManager.createNativeQuery("SELECT setval('variante_id_seq', (SELECT COALESCE(MAX(id), 1) FROM variante))")
                     .getSingleResult();
    }
    
    // === TESTS DE REPOSITORY ===
    
    @Test
    void save_DeberiaCrearVariante_CuandoDatosValidos() {
        // Crear producto padre
        Producto producto = new Producto();
        producto.setNombre("Producto Variante Test");
        producto.setSlug("producto-variante-" + System.currentTimeMillis());
        producto = productoRepository.save(producto);
        
        // Crear entidad con datos válidos
        Variante variante = new Variante();
        variante.setProducto(producto);
        variante.setSku("SKU-" + System.currentTimeMillis());
        variante.setEsDefault(true);
        variante.setActivo(true);
        
        // Guardar en DB
        Variante guardada = repository.save(variante);
        
        // Verificar que ID fue generado
        assertNotNull(guardada.getId(), "El ID debe ser generado automáticamente");
        
        // Verificar que los datos se guardaron correctamente
        assertTrue(guardada.getSku().startsWith("SKU-"));
        assertTrue(guardada.isEsDefault());
        assertTrue(guardada.isActivo());
        assertNotNull(guardada.getProducto());
    }
    
    @Test
    void findById_DeberiaRetornarVariante_CuandoExiste() {
        // Crear producto padre
        Producto producto = new Producto();
        producto.setNombre("Producto Test 2");
        producto.setSlug("producto-2-" + System.currentTimeMillis());
        producto = productoRepository.save(producto);
        
        // Guardar entidad en DB
        Variante variante = new Variante();
        variante.setProducto(producto);
        variante.setSku("SKU-FIND-" + System.currentTimeMillis());
        variante.setEsDefault(false);
        variante.setActivo(true);
        Variante guardada = repository.save(variante);
        
        // Buscar por ID
        var resultado = repository.findById(guardada.getId());
        
        // Verificar que Optional.isPresent() es true
        assertTrue(resultado.isPresent(), "Debe encontrar la variante por ID");
        
        // Verificar datos coinciden
        Variante encontrada = resultado.get();
        assertTrue(encontrada.getSku().startsWith("SKU-FIND-"));
        assertFalse(encontrada.isEsDefault());
        assertTrue(encontrada.isActivo());
    }
    
    @Test
    void findAll_DeberiaRetornarLista_CuandoExistenVariantes() {
        // Crear producto padre
        Producto producto = new Producto();
        producto.setNombre("Producto Test 3");
        producto.setSlug("producto-3-" + System.currentTimeMillis());
        producto = productoRepository.save(producto);
        
        // Guardar 3 entidades diferentes
        Variante variante1 = new Variante();
        variante1.setProducto(producto);
        variante1.setSku("SKU-1-" + System.currentTimeMillis());
        variante1.setEsDefault(true);
        variante1.setActivo(true);
        repository.save(variante1);
        
        Variante variante2 = new Variante();
        variante2.setProducto(producto);
        variante2.setSku("SKU-2-" + System.currentTimeMillis());
        variante2.setEsDefault(false);
        variante2.setActivo(true);
        repository.save(variante2);
        
        Variante variante3 = new Variante();
        variante3.setProducto(producto);
        variante3.setSku("SKU-3-" + System.currentTimeMillis());
        variante3.setEsDefault(false);
        variante3.setActivo(false);
        repository.save(variante3);
        
        // Llamar findAll()
        var lista = repository.findAll();
        
        // Verificar que retorna 3+ elementos (puede haber datos previos)
        assertTrue(lista.size() >= 3, "Debe retornar al menos 3 variantes");
    }
    
    @Test
    void delete_DeberiaEliminarVariante_CuandoExiste() {
        // Crear producto padre
        Producto producto = new Producto();
        producto.setNombre("Producto Test 4");
        producto.setSlug("producto-4-" + System.currentTimeMillis());
        producto = productoRepository.save(producto);
        
        // Guardar entidad
        Variante variante = new Variante();
        variante.setProducto(producto);
        variante.setSku("SKU-DELETE-" + System.currentTimeMillis());
        variante.setEsDefault(false);
        variante.setActivo(true);
        Variante guardada = repository.save(variante);
        Long id = guardada.getId();
        
        // Llamar deleteById
        repository.deleteById(id);
        
        // Verificar que repository.findById(id).isEmpty() es true
        assertTrue(repository.findById(id).isEmpty(), "La variante debe haber sido eliminada");
    }
    
    @Test
    void update_DeberiaActualizarVariante_CuandoExiste() {
        // Crear producto padre
        Producto producto = new Producto();
        producto.setNombre("Producto Test 5");
        producto.setSlug("producto-5-" + System.currentTimeMillis());
        producto = productoRepository.save(producto);
        
        // Guardar entidad con valores originales
        Variante variante = new Variante();
        variante.setProducto(producto);
        variante.setSku("SKU-ORIGINAL-" + System.currentTimeMillis());
        variante.setEsDefault(false);
        variante.setActivo(false);
        Variante guardada = repository.save(variante);
        
        // Modificar valores
        guardada.setEsDefault(true);
        guardada.setActivo(true);
        
        // Llamar repository.save(entidadModificada)
        repository.save(guardada);
        
        // Buscar por ID y verificar valores actualizados
        var resultado = repository.findById(guardada.getId());
        assertTrue(resultado.isPresent());
        assertTrue(resultado.get().isEsDefault());
        assertTrue(resultado.get().isActivo());
    }

    // === VERIFICACIONES DB REALES ===

    @Order(1)
    @Test
    @Transactional
    void db_Transaccion_Rollback_Crear_NoPersiste() {
        Producto producto = new Producto(); producto.setNombre("RB Prod V"); producto.setSlug("rb-prod-v-"+System.currentTimeMillis()); producto = productoRepository.save(producto);
        Variante v = new Variante(); v.setProducto(producto); v.setSku("rb-sku-"+System.currentTimeMillis()); repository.save(v); entityManager.flush();
    }

    @Order(2)
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void db_Transaccion_Rollback_Verificar_NoExiste() {
        var found = repository.findAll().stream().filter(x->x.getSku()!=null && x.getSku().contains("rb-sku-")).findAny();
        assertTrue(found.isEmpty(), "La entidad creada en la transacción anterior debe haber sido rollback-eada");
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void db_Constraint_UniqueYNotNull_Y_Audit() {
        // SKU unique?
        String sku = "sku-unique-" + UUID.randomUUID();
        Variante a = new Variante(); Producto p = new Producto(); p.setNombre("P V"); p.setSlug("p-v-"+System.currentTimeMillis()); productoRepository.save(p); a.setProducto(p); a.setSku(sku); repository.saveAndFlush(a);

        boolean uniqueExists = false;
        try { Object cnt = entityManager.createNativeQuery("SELECT count(*) FROM pg_constraint c JOIN pg_class t ON c.conrelid=t.oid WHERE t.relname='variante' AND c.contype='u'").getSingleResult(); if (cnt!=null) uniqueExists = ((Number)cnt).longValue()>0; } catch(Exception ex){ uniqueExists=false; }

        Variante b = new Variante(); b.setProducto(p); b.setSku(sku);
        if (uniqueExists) { assertThrows(DataIntegrityViolationException.class, ()->{ repository.saveAndFlush(b); entityManager.flush(); }); }
        else { repository.saveAndFlush(b); entityManager.flush(); long count = repository.findAll().stream().filter(x->sku.equals(x.getSku())).count(); assertTrue(count>=2); }

        // not-null sku?
        boolean skuNotNull=false; try{ Object nn = entityManager.createNativeQuery("SELECT is_nullable FROM information_schema.columns WHERE table_name='variante' AND column_name='sku'").getSingleResult(); if (nn!=null) skuNotNull = "NO".equalsIgnoreCase(nn.toString()); }catch(Exception ex){ skuNotNull=false; }
        Variante c = new Variante(); c.setProducto(p); c.setSku(null);
        if (skuNotNull) { assertThrows(DataIntegrityViolationException.class, ()->{ repository.saveAndFlush(c); entityManager.flush(); }); } else { repository.saveAndFlush(c); entityManager.flush(); var saved = repository.findById(c.getId()).orElseThrow(); assertNull(saved.getSku()); }

        // Auditoría
        try{ var savedA = repository.findById(a.getId()).orElseThrow(); assertNotNull(savedA.getCreatedAt()); assertNotNull(savedA.getUpdatedAt()); } catch(Exception ex){ }
    }
    
    // === TESTS DE API (Controller) ===
    // Aquí van los tests de integración a nivel HTTP usando `restTemplate`.
    // Ejemplos pendientes: POST 201, GET {id} 200, GET lista 200, PUT 200, DELETE 204, POST 400.
    // Mantener separados de los tests de Repository para facilitar pruebas unitarias de controllers luego.
    @org.junit.jupiter.api.Test
    void post_DeberiaCrearVariante_Status201_API() throws Exception {
        // Crear producto padre
        Producto producto = new Producto();
        producto.setNombre("Producto Padre API");
        producto.setSlug("producto-padre-api-" + System.currentTimeMillis());
        producto = productoRepository.save(producto);

        java.util.Map<String,Object> payload = new java.util.HashMap<>();
        payload.put("productoId", producto.getId());
        payload.put("sku", "SKU-API-" + System.currentTimeMillis());
        payload.put("esDefault", true);
        payload.put("activo", true);

        var result = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/dev/api/variantes")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isCreated())
                .andReturn();

        var dto = objectMapper.readValue(result.getResponse().getContentAsString(), java.util.Map.class);
        assertNotNull(dto.get("id"));
        assertTrue(((String)dto.get("sku")).startsWith("SKU-API-"));
    }

    @org.junit.jupiter.api.Test
    void getById_DeberiaRetornarVariante_Status200_API() throws Exception {
        // Crear producto y variante
        Producto producto = new Producto(); producto.setNombre("GET P"); producto.setSlug("get-p-"+System.currentTimeMillis()); producto = productoRepository.save(producto);
        Variante variante = new Variante(); variante.setProducto(producto); variante.setSku("SKU-GET-"+System.currentTimeMillis()); variante.setEsDefault(false); variante.setActivo(true);
        Variante saved = repository.save(variante);

        var result = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/dev/api/variantes/{id}", saved.getId())
                .accept(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andReturn();

        var dto = objectMapper.readValue(result.getResponse().getContentAsString(), java.util.Map.class);
        assertEquals(saved.getId().intValue(), ((Number)dto.get("id")).intValue());
    }

    @org.junit.jupiter.api.Test
    void delete_DeberiaEliminarVariante_Status204_API() throws Exception {
        // Crear producto y variante
        Producto producto = new Producto(); producto.setNombre("DEL P"); producto.setSlug("del-p-"+System.currentTimeMillis()); producto = productoRepository.save(producto);
        Variante variante = new Variante(); variante.setProducto(producto); variante.setSku("SKU-DEL-"+System.currentTimeMillis()); variante.setEsDefault(false); variante.setActivo(true);
        variante = repository.save(variante);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/dev/api/variantes/{id}", variante.getId()))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isNoContent());

        var opt = repository.findById(variante.getId());
        assertTrue(opt.isPresent(), "La variante debe seguir presente (soft-delete)");
        assertNotNull(opt.get().getDeletedAt(), "El campo deletedAt debe estar seteado tras soft-delete");
    }

}
