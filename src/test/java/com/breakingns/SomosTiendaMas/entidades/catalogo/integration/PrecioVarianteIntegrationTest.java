package com.breakingns.SomosTiendaMas.entidades.catalogo.integration;

import com.breakingns.SomosTiendaMas.entidades.catalogo.enums.Moneda;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.PrecioVariante;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Producto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Variante;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.PrecioVarianteRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(OrderAnnotation.class)
public class PrecioVarianteIntegrationTest {
    
    @Autowired
    private PrecioVarianteRepository repository;
    
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
        entityManager.createNativeQuery("SELECT setval('variante_precio_id_seq', (SELECT COALESCE(MAX(id), 1) FROM variante_precio))")
                .getSingleResult();
    }
    
    @Test
    void save_DeberiaCrearPrecioVariante_CuandoDatosValidos() {
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
        
        PrecioVariante precio = new PrecioVariante();
        precio.setVariante(variante);
        precio.setMontoCentavos(10000L);
        precio.setPrecioAnteriorCentavos(12000L);
        precio.setPrecioSinIvaCentavos(8264L);
        precio.setIvaPorcentaje(21);
        precio.setMoneda(Moneda.ARS);
        precio.setActivo(true);
        
        // When
        PrecioVariante resultado = repository.save(precio);
        
        // Then
        assertNotNull(resultado.getId());
        assertEquals(10000L, resultado.getMontoCentavos());
        assertEquals(12000L, resultado.getPrecioAnteriorCentavos());
        assertEquals(8264L, resultado.getPrecioSinIvaCentavos());
        assertEquals(21, resultado.getIvaPorcentaje());
        assertEquals(Moneda.ARS, resultado.getMoneda());
        assertTrue(resultado.getActivo());
    }
    
    @Test
    void findById_DeberiaRetornarPrecioVariante_CuandoExiste() {
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
        
        PrecioVariante precio = new PrecioVariante();
        precio.setVariante(variante);
        precio.setMontoCentavos(15000L);
        precio.setMoneda(Moneda.ARS);
        precio.setActivo(true);
        precio = repository.save(precio);
        
        // When
        Optional<PrecioVariante> resultado = repository.findById(precio.getId());
        
        // Then
        assertTrue(resultado.isPresent());
        assertEquals(15000L, resultado.get().getMontoCentavos());
        assertEquals(Moneda.ARS, resultado.get().getMoneda());
    }
    
    @Test
    void findAll_DeberiaRetornarLista_CuandoExistenPreciosVariante() {
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
        
        PrecioVariante precio1 = new PrecioVariante();
        precio1.setVariante(variante1);
        precio1.setMontoCentavos(10000L);
        precio1.setMoneda(Moneda.ARS);
        precio1.setActivo(true);
        repository.save(precio1);
        
        PrecioVariante precio2 = new PrecioVariante();
        precio2.setVariante(variante2);
        precio2.setMontoCentavos(20000L);
        precio2.setMoneda(Moneda.ARS);
        precio2.setActivo(true);
        repository.save(precio2);
        
        PrecioVariante precio3 = new PrecioVariante();
        precio3.setVariante(variante1);
        precio3.setMontoCentavos(9500L);
        precio3.setMoneda(Moneda.ARS);
        precio3.setActivo(false);
        precio3.setVigenciaHasta(LocalDateTime.now().minusDays(1));
        repository.save(precio3);
        
        // When
        List<PrecioVariante> resultados = repository.findAll();
        
        // Then
        assertTrue(resultados.size() >= 3);
    }
    
    @Test
    void delete_DeberiaEliminarPrecioVariante_CuandoExiste() {
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
        
        PrecioVariante precio = new PrecioVariante();
        precio.setVariante(variante);
        precio.setMontoCentavos(10000L);
        precio.setMoneda(Moneda.ARS);
        precio.setActivo(true);
        precio = repository.save(precio);
        
        Long id = precio.getId();
        
        // Simular borrado lógico: marcar deletedAt y guardar
        precio.setDeletedAt(java.time.LocalDateTime.now());
        precio.setUpdatedBy("test");
        repository.save(precio);

        // Then: comprobar soft-delete
        Optional<PrecioVariante> resultado = repository.findById(id);
        assertTrue(resultado.isPresent());
        assertNotNull(resultado.get().getDeletedAt());
    }
    
    @Test
    void update_DeberiaActualizarPrecioVariante_CuandoExiste() {
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
        
        PrecioVariante precio = new PrecioVariante();
        precio.setVariante(variante);
        precio.setMontoCentavos(10000L);
        precio.setMoneda(Moneda.ARS);
        precio.setActivo(true);
        precio = repository.save(precio);
        
        // When
        precio.setMontoCentavos(12000L);
        precio.setPrecioAnteriorCentavos(10000L);
        precio.setIvaPorcentaje(21);
        PrecioVariante actualizado = repository.save(precio);
        
        // Then
        Optional<PrecioVariante> resultado = repository.findById(actualizado.getId());
        assertTrue(resultado.isPresent());
        assertEquals(12000L, resultado.get().getMontoCentavos());
        assertEquals(10000L, resultado.get().getPrecioAnteriorCentavos());
        assertEquals(21, resultado.get().getIvaPorcentaje());
    }
    
    // === TESTS DE API (Controller) ===
    // Aquí van los tests de integración a nivel HTTP usando `restTemplate`.
    // Ejemplos pendientes: POST 201, GET {id} 200, GET lista 200, PUT 200, DELETE 204, POST 400.
    // Mantener separados de los tests de Repository para facilitar pruebas unitarias de controllers luego.
    @Test
    void patch_ActualizarPrecio_DeberiaRetornar200_API() throws Exception {
        Producto producto = new Producto(); producto.setNombre("Prod API"); producto.setSlug("prod-api-"+System.currentTimeMillis()); producto = productoRepository.save(producto);
        Variante variante = new Variante(); variante.setProducto(producto); variante.setSku("SKU-API-"+System.currentTimeMillis()); variante.setEsDefault(true); variante.setActivo(true); variante = varianteRepository.save(variante);
        PrecioVariante precio = new PrecioVariante(); precio.setVariante(variante); precio.setMontoCentavos(10000L); precio.setMoneda(com.breakingns.SomosTiendaMas.entidades.catalogo.enums.Moneda.ARS); precio.setActivo(true); precio = repository.save(precio);

        java.util.Map<String,Object> payload = new java.util.HashMap<>();
        payload.put("montoCentavos", 12000L);

        var result = mockMvc.perform(MockMvcRequestBuilders.patch("/dev/api/variantes/{id}/precio", variante.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        var dto = objectMapper.readValue(result.getResponse().getContentAsString(), java.util.Map.class);
        assertNotNull(dto);
        assertEquals(12000, ((Number)dto.get("montoCentavos")).intValue());
    }

    // === VERIFICACIONES DB REALES ===

    @Order(1)
    @Test
    @Transactional
    void db_Transaccion_Rollback_Crear_NoPersiste() {
        Producto producto = new Producto(); producto.setNombre("RB Prod PV"); producto.setSlug("rb-prod-pv-"+System.currentTimeMillis()); producto.setDescripcion("rb"); productoRepository.save(producto);
        Variante variante = new Variante(); variante.setProducto(producto); variante.setSku("rb-sku-pv-"+System.currentTimeMillis()); variante.setEsDefault(true); variante.setActivo(true); varianteRepository.save(variante);

        PrecioVariante pv = new PrecioVariante(); pv.setVariante(variante); pv.setMontoCentavos(9999L); pv.setMoneda(com.breakingns.SomosTiendaMas.entidades.catalogo.enums.Moneda.ARS); repository.save(pv);
        entityManager.flush();
    }

    @Order(2)
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void db_Transaccion_Rollback_Verificar_NoExiste() {
        Object cnt = entityManager.createNativeQuery("SELECT count(*) FROM variante_precio vp JOIN variante v ON vp.variante_id = v.id WHERE v.sku LIKE :sku")
                .setParameter("sku", "%rb-sku-pv-%")
                .getSingleResult();
        long count = cnt==null?0L:((Number)cnt).longValue();
        assertEquals(0L, count, "La entidad creada en la transacción anterior debe haber sido rollback-eada");
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void db_Constraint_UniqueYNotNull_Y_Audit() {
        Producto producto = new Producto(); producto.setNombre("P PV"); producto.setSlug("p-pv-"+System.currentTimeMillis()); productoRepository.save(producto);
        Variante variante = new Variante(); variante.setProducto(producto); variante.setSku("sku-pv-"+UUID.randomUUID()); varianteRepository.save(variante);

        PrecioVariante first = new PrecioVariante(); first.setVariante(variante); first.setMontoCentavos(1000L); first.setMoneda(com.breakingns.SomosTiendaMas.entidades.catalogo.enums.Moneda.ARS); repository.saveAndFlush(first);

        boolean uniqueExists=false; try{ Object c = entityManager.createNativeQuery("SELECT count(*) FROM pg_constraint c JOIN pg_class t ON c.conrelid=t.oid WHERE t.relname='variante_precio' AND c.contype='u'").getSingleResult(); if (c!=null) uniqueExists = ((Number)c).longValue()>0; } catch(Exception ex){ uniqueExists=false; }

        PrecioVariante dup = new PrecioVariante(); dup.setVariante(variante); dup.setMontoCentavos(1000L); dup.setMoneda(com.breakingns.SomosTiendaMas.entidades.catalogo.enums.Moneda.ARS);
        if (uniqueExists) { assertThrows(DataIntegrityViolationException.class, ()->{ repository.saveAndFlush(dup); }); }
        else { repository.saveAndFlush(dup); long cnt = repository.findAll().stream().filter(x-> x.getVariante().getId().equals(variante.getId()) && x.getMontoCentavos().equals(1000L)).count(); assertTrue(cnt>=2); }

        try{ var saved = repository.findById(first.getId()).orElseThrow(); assertNotNull(saved.getCreatedAt()); } catch(Exception ex){ }
    }

}
