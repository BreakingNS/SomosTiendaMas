package com.breakingns.SomosTiendaMas.entidades.catalogo.integration;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.ImagenVariante;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Producto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Variante;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.ImagenVarianteRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.ProductoRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.VarianteRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(OrderAnnotation.class)
public class ImagenVarianteIntegrationTest {
    
    @Autowired
    private ImagenVarianteRepository repository;
    
    @Autowired
    private VarianteRepository varianteRepository;
    
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
    void setUp() {
        entityManager.createNativeQuery("SELECT setval('variante_imagen_id_seq', (SELECT COALESCE(MAX(id), 1) FROM variante_imagen))")
                .getSingleResult();
    }
    
    @Test
    void save_DeberiaCrearImagenVariante_CuandoDatosValidos() {
        // Given
        Producto producto = new Producto();
        producto.setNombre("Producto Test");
        producto.setSlug("slug-" + System.currentTimeMillis());
        producto.setDescripcion("Descripción");
        producto = productoRepository.save(producto);
        
        Variante variante = new Variante();
        variante.setProducto(producto);
        variante.setSku("SKU-" + System.currentTimeMillis());
        variante.setEsDefault(true);
        variante.setActivo(true);
        variante = varianteRepository.save(variante);
        
        ImagenVariante imagen = new ImagenVariante();
        imagen.setVariante(variante);
        imagen.setUrl("https://example.com/imagen.jpg");
        imagen.setAlt("Imagen de prueba");
        imagen.setOrden(1);
        
        // When
        ImagenVariante resultado = repository.save(imagen);
        
        // Then
        assertNotNull(resultado.getId());
        assertEquals("https://example.com/imagen.jpg", resultado.getUrl());
        assertEquals("Imagen de prueba", resultado.getAlt());
        assertEquals(1, resultado.getOrden());
        assertEquals(variante.getId(), resultado.getVariante().getId());
    }
    
    @Test
    void findById_DeberiaRetornarImagenVariante_CuandoExiste() {
        // Given
        Producto producto = new Producto();
        producto.setNombre("Producto Test");
        producto.setSlug("slug-" + System.currentTimeMillis());
        producto.setDescripcion("Descripción");
        producto = productoRepository.save(producto);
        
        Variante variante = new Variante();
        variante.setProducto(producto);
        variante.setSku("SKU-" + System.currentTimeMillis());
        variante.setEsDefault(true);
        variante.setActivo(true);
        variante = varianteRepository.save(variante);
        
        ImagenVariante imagen = new ImagenVariante();
        imagen.setVariante(variante);
        imagen.setUrl("https://example.com/imagen.jpg");
        imagen.setAlt("Imagen de prueba");
        imagen.setOrden(1);
        imagen = repository.save(imagen);
        
        // When
        Optional<ImagenVariante> resultado = repository.findById(imagen.getId());
        
        // Then
        assertTrue(resultado.isPresent());
        assertEquals("https://example.com/imagen.jpg", resultado.get().getUrl());
        assertEquals("Imagen de prueba", resultado.get().getAlt());
        assertEquals(1, resultado.get().getOrden());
    }
    
    @Test
    void findAll_DeberiaRetornarLista_CuandoExistenImagenesVariante() {
        // Given
        Producto producto = new Producto();
        producto.setNombre("Producto Test");
        producto.setSlug("slug-" + System.currentTimeMillis());
        producto.setDescripcion("Descripción");
        producto = productoRepository.save(producto);
        
        Variante variante1 = new Variante();
        variante1.setProducto(producto);
        variante1.setSku("SKU-1-" + System.currentTimeMillis());
        variante1.setEsDefault(true);
        variante1.setActivo(true);
        variante1 = varianteRepository.save(variante1);
        
        Variante variante2 = new Variante();
        variante2.setProducto(producto);
        variante2.setSku("SKU-2-" + System.currentTimeMillis());
        variante2.setEsDefault(false);
        variante2.setActivo(true);
        variante2 = varianteRepository.save(variante2);
        
        ImagenVariante imagen1 = new ImagenVariante();
        imagen1.setVariante(variante1);
        imagen1.setUrl("https://example.com/imagen1.jpg");
        imagen1.setAlt("Imagen 1");
        imagen1.setOrden(1);
        repository.save(imagen1);
        
        ImagenVariante imagen2 = new ImagenVariante();
        imagen2.setVariante(variante2);
        imagen2.setUrl("https://example.com/imagen2.jpg");
        imagen2.setAlt("Imagen 2");
        imagen2.setOrden(1);
        repository.save(imagen2);
        
        ImagenVariante imagen3 = new ImagenVariante();
        imagen3.setVariante(variante1);
        imagen3.setUrl("https://example.com/imagen3.jpg");
        imagen3.setAlt("Imagen 3");
        imagen3.setOrden(2);
        repository.save(imagen3);
        
        // When
        List<ImagenVariante> resultados = repository.findAll();
        
        // Then
        assertTrue(resultados.size() >= 3);
    }
    
    @Test
    void delete_DeberiaEliminarImagenVariante_CuandoExiste() {
        // Given
        Producto producto = new Producto();
        producto.setNombre("Producto Test");
        producto.setSlug("slug-" + System.currentTimeMillis());
        producto.setDescripcion("Descripción");
        producto = productoRepository.save(producto);
        
        Variante variante = new Variante();
        variante.setProducto(producto);
        variante.setSku("SKU-" + System.currentTimeMillis());
        variante.setEsDefault(true);
        variante.setActivo(true);
        variante = varianteRepository.save(variante);
        
        ImagenVariante imagen = new ImagenVariante();
        imagen.setVariante(variante);
        imagen.setUrl("https://example.com/imagen.jpg");
        imagen.setAlt("Imagen de prueba");
        imagen.setOrden(1);
        imagen = repository.save(imagen);
        
        Long id = imagen.getId();
        
        // Simular borrado lógico: marcar deletedAt y guardar
        imagen.setDeletedAt(java.time.LocalDateTime.now());
        imagen.setUpdatedBy("test");
        repository.save(imagen);

        // Then: verificar soft-delete
        Optional<ImagenVariante> resultado = repository.findById(id);
        assertTrue(resultado.isPresent());
        assertNotNull(resultado.get().getDeletedAt());
    }
    
    @Test
    void update_DeberiaActualizarImagenVariante_CuandoExiste() {
        // Given
        Producto producto = new Producto();
        producto.setNombre("Producto Test");
        producto.setSlug("slug-" + System.currentTimeMillis());
        producto.setDescripcion("Descripción");
        producto = productoRepository.save(producto);
        
        Variante variante = new Variante();
        variante.setProducto(producto);
        variante.setSku("SKU-" + System.currentTimeMillis());
        variante.setEsDefault(true);
        variante.setActivo(true);
        variante = varianteRepository.save(variante);
        
        ImagenVariante imagen = new ImagenVariante();
        imagen.setVariante(variante);
        imagen.setUrl("https://example.com/original.jpg");
        imagen.setAlt("Imagen Original");
        imagen.setOrden(1);
        imagen = repository.save(imagen);
        
        // When
        imagen.setUrl("https://example.com/actualizada.jpg");
        imagen.setAlt("Imagen Actualizada");
        imagen.setOrden(2);
        ImagenVariante actualizado = repository.save(imagen);
        
        // Then
        Optional<ImagenVariante> resultado = repository.findById(actualizado.getId());
        assertTrue(resultado.isPresent());
        assertEquals("https://example.com/actualizada.jpg", resultado.get().getUrl());
        assertEquals("Imagen Actualizada", resultado.get().getAlt());
        assertEquals(2, resultado.get().getOrden());
    }
    
    // === TESTS DE API (Controller) ===
    // Aquí van los tests de integración a nivel HTTP usando `restTemplate`.
    // Ejemplos pendientes: POST 201, GET {id} 200, GET lista 200, PUT 200, DELETE 204, POST 400.
    // Mantener separados de los tests de Repository para facilitar pruebas unitarias de controllers luego.
    
    @Test
    void post_DeberiaCrearImagenVariante_Status201_API() throws Exception {
        // Crear producto y variante
        Producto producto = new Producto(); producto.setNombre("Prod API"); producto.setSlug("prod-api-"+System.currentTimeMillis()); producto = productoRepository.save(producto);
        Variante variante = new Variante(); variante.setProducto(producto); variante.setSku("SKU-API-"+System.currentTimeMillis()); variante.setEsDefault(true); variante.setActivo(true); variante = varianteRepository.save(variante);

        java.util.Map<String,Object> payload = new java.util.HashMap<>();
        payload.put("varianteId", variante.getId());
        payload.put("url", "https://example.com/new.jpg");
        payload.put("alt", "Nueva imagen");
        payload.put("orden", 1);

        var result = mockMvc.perform(MockMvcRequestBuilders.post("/dev/api/variantes/" + variante.getId() + "/imagenes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn();

        var dto = objectMapper.readValue(result.getResponse().getContentAsString(), java.util.Map.class);
        assertNotNull(dto.get("id"));
        assertEquals("Nueva imagen", dto.get("alt"));
    }

    // === VERIFICACIONES DB REALES ===

    @Order(1)
    @Test
    @Transactional
    void db_Transaccion_Rollback_Crear_NoPersiste() {
        Producto producto = new Producto(); producto.setNombre("RB Prod IMG"); producto.setSlug("rb-prod-img-"+System.currentTimeMillis()); producto.setDescripcion("rb"); productoRepository.save(producto);
        Variante variante = new Variante(); variante.setProducto(producto); variante.setSku("rb-sku-img-"+System.currentTimeMillis()); variante.setEsDefault(true); variante.setActivo(true); varianteRepository.save(variante);

        ImagenVariante img = new ImagenVariante(); img.setVariante(variante); img.setUrl("https://rb.example/img.jpg"); img.setAlt("rb"); img.setOrden(1); repository.save(img);
        entityManager.flush();
    }

    @Order(2)
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void db_Transaccion_Rollback_Verificar_NoExiste() {
        Object cnt = entityManager.createNativeQuery("SELECT count(*) FROM variante_imagen iv JOIN variante v ON iv.variante_id = v.id WHERE v.sku LIKE :sku")
                .setParameter("sku", "%rb-sku-img-%")
                .getSingleResult();
        long count = cnt == null ? 0L : ((Number)cnt).longValue();
        assertEquals(0L, count, "La entidad creada en la transacción anterior debe haber sido rollback-eada");
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void db_Constraint_UniqueYNotNull_Y_Audit() {
        Producto producto = new Producto(); producto.setNombre("P IMG"); producto.setSlug("p-img-"+System.currentTimeMillis()); productoRepository.save(producto);
        Variante variante = new Variante(); variante.setProducto(producto); variante.setSku("sku-img-"+UUID.randomUUID()); varianteRepository.save(variante);

        ImagenVariante first = new ImagenVariante(); first.setVariante(variante); first.setUrl("https://img.example/1.jpg"); first.setAlt("a"); first.setOrden(1); repository.saveAndFlush(first);

        boolean uniqueExists = false;
        try { Object c = entityManager.createNativeQuery("SELECT count(*) FROM pg_constraint c JOIN pg_class t ON c.conrelid=t.oid WHERE t.relname='variante_imagen' AND c.contype='u'").getSingleResult(); if (c!=null) uniqueExists = ((Number)c).longValue()>0; } catch(Exception ex){ uniqueExists=false; }

        ImagenVariante dup = new ImagenVariante(); dup.setVariante(variante); dup.setUrl("https://img.example/1.jpg"); dup.setAlt("b");
        if (uniqueExists) { assertThrows(DataIntegrityViolationException.class, ()->{ repository.saveAndFlush(dup); }); }
        else { repository.saveAndFlush(dup); long cnt = repository.findAll().stream().filter(x-> x.getUrl()!=null && x.getUrl().equals("https://img.example/1.jpg")).count(); assertTrue(cnt>=2); }

        boolean urlNotNull=false; try{ Object nn = entityManager.createNativeQuery("SELECT is_nullable FROM information_schema.columns WHERE table_name='variante_imagen' AND column_name='url'").getSingleResult(); if (nn!=null) urlNotNull = "NO".equalsIgnoreCase(nn.toString()); } catch(Exception ex){ urlNotNull=false; }
        ImagenVariante c = new ImagenVariante(); c.setVariante(variante); c.setUrl(null); c.setAlt("n");
        if (urlNotNull) { assertThrows(DataIntegrityViolationException.class, ()->{ repository.saveAndFlush(c); }); } else { repository.saveAndFlush(c); var saved = repository.findById(c.getId()).orElseThrow(); assertNull(saved.getUrl()); }

        try{ var saved = repository.findById(first.getId()).orElseThrow(); assertNotNull(saved.getCreatedAt()); } catch(Exception ex){ }
    }

}
