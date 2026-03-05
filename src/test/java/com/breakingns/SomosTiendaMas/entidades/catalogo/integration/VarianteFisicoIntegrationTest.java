package com.breakingns.SomosTiendaMas.entidades.catalogo.integration;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Producto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Variante;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.VarianteFisico;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.ProductoRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.VarianteFisicoRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.VarianteRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.OpcionRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.OpcionValorRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Opcion;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.OpcionValor;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.springframework.transaction.annotation.Propagation;
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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.test.annotation.Rollback;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc(addFilters = false)
@SqlGroup({
    @Sql(statements = {
        "TRUNCATE TABLE variante_precio, variante_imagen, variante_opcion_valor, variante_opcion, variante_valor, variante_fisico, variante, producto RESTART IDENTITY CASCADE;"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
    @Sql(statements = {
        "TRUNCATE TABLE variante_precio, variante_imagen, variante_opcion_valor, variante_opcion, variante_valor, variante_fisico, variante, producto RESTART IDENTITY CASCADE;"
    })
})
@ActiveProfiles("test")
@TestMethodOrder(OrderAnnotation.class)
public class VarianteFisicoIntegrationTest {
    
    @Autowired
    private VarianteFisicoRepository repository;
    
    @Autowired
    private VarianteRepository varianteRepository;
    
    @Autowired
    private ProductoRepository productoRepository;
    
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OpcionRepository opcionRepository;

    @Autowired
    private OpcionValorRepository opcionValorRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Autowired
    private PlatformTransactionManager transactionManager;
    
    @BeforeEach
    void setUp() {
        entityManager.createNativeQuery("SELECT setval('variante_fisico_id_seq', (SELECT COALESCE(MAX(id), 1) FROM variante_fisico))")
                .getSingleResult();
    }

    @AfterEach
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void cleanupTestData() {
        // patrones usados por los tests para identificar filas creadas durante testing
        String[] skuPatterns = new String[]{"%rb-sku-vf-%", "%sku-vf-%", "DBG-SKU-%", "%SKU-API-%", "%rb-prod-vf-%", "%prod-api-%", "%p-vf-%"};
        for (String p : skuPatterns) {
            try {
                entityManager.createNativeQuery("DELETE FROM variante_fisico vf USING variante v WHERE vf.variante_id = v.id AND v.sku LIKE :p")
                        .setParameter("p", p)
                        .executeUpdate();
            } catch (Exception e) { }
            try {
                entityManager.createNativeQuery("DELETE FROM variante WHERE sku LIKE :p")
                        .setParameter("p", p)
                        .executeUpdate();
            } catch (Exception e) { }
            try {
                entityManager.createNativeQuery("DELETE FROM producto WHERE slug LIKE :p OR nombre LIKE :p")
                        .setParameter("p", p)
                        .executeUpdate();
            } catch (Exception e) { }
        }
    }
    
    @Test
    void save_DeberiaCrearVarianteFisico_CuandoDatosValidos() {
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
        
        VarianteFisico fisico = new VarianteFisico();
        fisico.setVariante(variante);
        fisico.setWidthMm(200);
        fisico.setHeightMm(100);
        fisico.setDepthMm(50);
        fisico.setWeightGrams(500);
        fisico.setPackageWidthMm(220);
        fisico.setPackageHeightMm(120);
        fisico.setPackageDepthMm(70);
        fisico.setPackageWeightGrams(600);
        
        // When
        VarianteFisico resultado = repository.save(fisico);
        
        // Then
        assertNotNull(resultado.getId());
        assertEquals(200, resultado.getWidthMm());
        assertEquals(100, resultado.getHeightMm());
        assertEquals(50, resultado.getDepthMm());
        assertEquals(500, resultado.getWeightGrams());
        assertEquals(220, resultado.getPackageWidthMm());
        assertEquals(120, resultado.getPackageHeightMm());
        assertEquals(70, resultado.getPackageDepthMm());
        assertEquals(600, resultado.getPackageWeightGrams());
    }
    
    @Test
    void save_NonDefaultVariante_PermiteNulls() {
        Producto producto = new Producto(); producto.setNombre("P ND"); producto.setSlug("p-nd-"+System.currentTimeMillis()); productoRepository.save(producto);
        Variante variante = new Variante(); variante.setProducto(producto); variante.setSku("sku-nd-"+UUID.randomUUID()); variante.setEsDefault(false); variante.setActivo(true); variante = varianteRepository.save(variante);

        VarianteFisico c = new VarianteFisico(); c.setVariante(variante); c.setWidthMm(null); c.setHeightMm(null);
        repository.saveAndFlush(c);
        var saved = repository.findById(c.getId()).orElseThrow();
        assertNull(saved.getWidthMm());
    }

    @Test
    void findById_DeberiaRetornarVarianteFisico_CuandoExiste() {
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
        
        VarianteFisico fisico = new VarianteFisico();
        fisico.setVariante(variante);
        fisico.setWidthMm(300);
        fisico.setHeightMm(150);
        fisico.setDepthMm(80);
        fisico.setWeightGrams(1000);
        fisico.setPackageWidthMm(320);
        fisico.setPackageHeightMm(170);
        fisico.setPackageDepthMm(90);
        fisico.setPackageWeightGrams(1100);
        fisico = repository.save(fisico);
        
        // When
        Optional<VarianteFisico> resultado = repository.findById(fisico.getId());
        
        // Then
        assertTrue(resultado.isPresent());
        assertEquals(300, resultado.get().getWidthMm());
        assertEquals(150, resultado.get().getHeightMm());
        assertEquals(80, resultado.get().getDepthMm());
        assertEquals(1000, resultado.get().getWeightGrams());
    }
    
    @Test
    void findAll_DeberiaRetornarLista_CuandoExistenVariantesFisico() {
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
        
        Variante variante3 = new Variante();
        variante3.setProducto(producto);
        variante3.setSku("SKU-3-" + System.currentTimeMillis());
        variante3.setEsDefault(false);
        variante3.setActivo(true);
        variante3 = varianteRepository.save(variante3);
        
        VarianteFisico fisico1 = new VarianteFisico();
        fisico1.setVariante(variante1);
        fisico1.setWidthMm(100);
        fisico1.setHeightMm(50);
        fisico1.setDepthMm(25);
        fisico1.setWeightGrams(200);
        fisico1.setPackageWidthMm(120);
        fisico1.setPackageHeightMm(60);
        fisico1.setPackageDepthMm(30);
        fisico1.setPackageWeightGrams(220);
        repository.save(fisico1);
        
        VarianteFisico fisico2 = new VarianteFisico();
        fisico2.setVariante(variante2);
        fisico2.setWidthMm(200);
        fisico2.setHeightMm(100);
        fisico2.setDepthMm(50);
        fisico2.setWeightGrams(500);
        repository.save(fisico2);
        
        VarianteFisico fisico3 = new VarianteFisico();
        fisico3.setVariante(variante3);
        fisico3.setWidthMm(300);
        fisico3.setHeightMm(150);
        fisico3.setDepthMm(75);
        fisico3.setWeightGrams(1000);
        repository.save(fisico3);
        
        // When
        List<VarianteFisico> resultados = repository.findAll();
        
        // Then
        assertTrue(resultados.size() >= 3);
    }
    
    @Test
    void delete_DeberiaEliminarVarianteFisico_CuandoExiste() {
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
        
        VarianteFisico fisico = new VarianteFisico();
        fisico.setVariante(variante);
        fisico.setWidthMm(150);
        fisico.setHeightMm(75);
        fisico.setDepthMm(40);
        fisico.setWeightGrams(300);
        fisico.setPackageWidthMm(170);
        fisico.setPackageHeightMm(85);
        fisico.setPackageDepthMm(45);
        fisico.setPackageWeightGrams(320);
        fisico = repository.save(fisico);
        
        Long id = fisico.getId();
        
        // Simular borrado lógico: marcar deletedAt y guardar
        fisico.setDeletedAt(LocalDateTime.now());
        fisico.setUpdatedBy("test");
        repository.save(fisico);

        // Then: verificar soft-delete
        Optional<VarianteFisico> resultado = repository.findById(id);
        assertTrue(resultado.isPresent());
        assertNotNull(resultado.get().getDeletedAt());
    }
    
    @Test
    void update_DeberiaActualizarVarianteFisico_CuandoExiste() {
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
        
        VarianteFisico fisico = new VarianteFisico();
        fisico.setVariante(variante);
        fisico.setWidthMm(100);
        fisico.setHeightMm(50);
        fisico.setDepthMm(25);
        fisico.setWeightGrams(200);
        fisico.setPackageWidthMm(120);
        fisico.setPackageHeightMm(60);
        fisico.setPackageDepthMm(30);
        fisico.setPackageWeightGrams(220);
        fisico = repository.save(fisico);
        
        // When
        fisico.setWidthMm(120);
        fisico.setHeightMm(60);
        fisico.setDepthMm(30);
        fisico.setWeightGrams(250);
        fisico.setPackageWidthMm(140);
        fisico.setPackageHeightMm(80);
        VarianteFisico actualizado = repository.save(fisico);
        
        // Then
        Optional<VarianteFisico> resultado = repository.findById(actualizado.getId());
        assertTrue(resultado.isPresent());
        assertEquals(120, resultado.get().getWidthMm());
        assertEquals(60, resultado.get().getHeightMm());
        assertEquals(30, resultado.get().getDepthMm());
        assertEquals(250, resultado.get().getWeightGrams());
        assertEquals(140, resultado.get().getPackageWidthMm());
        assertEquals(80, resultado.get().getPackageHeightMm());
    }

    @Test
    void debug_VerificarVarianteFisicoNoExisteDespuesCrearVariante() {
        Producto producto = new Producto(); producto.setNombre("DBG Prod"); producto.setSlug("dbg-prod-"+System.currentTimeMillis()); producto = productoRepository.save(producto);
        Variante variante = new Variante(); variante.setProducto(producto); variante.setSku("DBG-SKU-"+System.currentTimeMillis()); variante.setEsDefault(true); variante.setActivo(true); variante = varianteRepository.save(variante);

        var encontrado = repository.findByVariante_Id(variante.getId());
        assertFalse(encontrado.isPresent(), "Diagnóstico: ya existe un VarianteFisico para la variante creada (posible auto-creación del sistema)");
    }

    @Test
    void save_DefaultVariante_NoPermiteNulls() {
        Producto producto = new Producto(); producto.setNombre("P DEF"); producto.setSlug("p-def-"+System.currentTimeMillis()); productoRepository.save(producto);
        Variante variante = new Variante(); variante.setProducto(producto); variante.setSku("sku-def-"+UUID.randomUUID()); variante.setEsDefault(true); variante.setActivo(true); variante = varianteRepository.save(variante);

        VarianteFisico c = new VarianteFisico(); c.setVariante(variante); c.setWidthMm(null); c.setHeightMm(1);
        assertThrows(RuntimeException.class, () -> { repository.saveAndFlush(c); });
    }

    @Test
    void crearProductoCentralizado_ConDefaultYSubrecursos_CreaVarianteFisicoDefault() throws Exception {
        // Crear opción y valor para poder referenciarlos en el payload
        Opcion opcion = new Opcion(); opcion.setNombre("Color Test"); opcion.setOrden(1); opcion.setTipo("select"); opcion = opcionRepository.save(opcion);
        OpcionValor ov = new OpcionValor(); ov.setOpcion(opcion); ov.setValor("Rojo"); ov.setSlug("rojo-test-"+System.currentTimeMillis()); ov.setOrden(1); ov = opcionValorRepository.save(ov);

        // Construir payload JSON para ProductoCentralizadoCrearDTO
        Map<String,Object> producto = new HashMap<>();
        producto.put("nombre", "Prod Full");
        producto.put("slug", "prod-full-"+System.currentTimeMillis());
        producto.put("descripcion", "desc full");

        Map<String,Object> variante = new HashMap<>();
        variante.put("sku", "sku-full-"+UUID.randomUUID());
        variante.put("esDefault", true);
        variante.put("activo", true);

        Map<String,Object> precio = new HashMap<>();
        precio.put("montoCentavos", 12345);
        precio.put("activo", true);
        // La columna 'moneda' en la tabla variante_precio es NOT NULL en el esquema;
        // incluirla en el payload para evitar violación de constraint en inserciones.
        precio.put("moneda", "ARS");
        variante.put("precios", List.of(precio));

        Map<String,Object> phys = new HashMap<>();
        phys.put("widthMm", 50);
        phys.put("heightMm", 20);
        phys.put("depthMm", 10);
        phys.put("weightGrams", 300);
        phys.put("packageWidthMm", 55);
        phys.put("packageHeightMm", 25);
        phys.put("packageDepthMm", 15);
        phys.put("packageWeightGrams", 350);
        variante.put("physical", List.of(phys));

        Map<String,Object> img = new HashMap<>();
        img.put("url","http://img.test/1.jpg"); img.put("alt","img1"); img.put("orden",1);
        variante.put("imagenes", List.of(img));

        Map<String,Object> opcionSel = new HashMap<>();
        opcionSel.put("opcionId", opcion.getId());
        opcionSel.put("opcionValorIds", List.of(ov.getId()));
        Map<String,Object> vop = new HashMap<>();
        vop.put("opciones", List.of(opcionSel));
        variante.put("varianteOpciones", List.of(vop));

        Map<String,Object> payload = new HashMap<>();
        payload.put("producto", producto);
        payload.put("variantes", List.of(variante));

        var mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/dev/api/productos/centralizado")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(payload)))
            .andReturn();

        int status = mvcResult.getResponse().getStatus();
        String body = mvcResult.getResponse().getContentAsString();
        if (status != 201) {
            fail("Expected 201 Created but got " + status + ". Response body: " + body);
        }

        var resp = objectMapper.readValue(body, Map.class);
        // obtener id de la variante creada
        var vars = (List<?>) resp.get("variantes");
        assertNotNull(vars);
        assertFalse(vars.isEmpty());
        var firstVar = (Map<?,?>) vars.get(0);
        Long vid = ((Number) firstVar.get("id")).longValue();

        // verificar que existe VarianteFisico con datos para la variante default
        var opt = repository.findByVariante_Id(vid);
        assertTrue(opt.isPresent(), "VarianteFisico debe haberse creado para la variante default");
        VarianteFisico vf = opt.get();
        assertEquals(50, vf.getWidthMm());
        assertEquals(20, vf.getHeightMm());
        assertEquals(10, vf.getDepthMm());
        assertEquals(300, vf.getWeightGrams());
    }
    
    // === TESTS DE API (Controller) ===
    // Aquí van los tests de integración a nivel HTTP usando `restTemplate`.
    // Ejemplos pendientes: POST 201, GET {id} 200, GET lista 200, PUT 200, DELETE 204, POST 400.
    // Mantener separados de los tests de Repository para facilitar pruebas unitarias de controllers luego.

    @Test
    void put_CrearActualizarVarianteFisico_Status200_API() throws Exception {
        Producto producto = new Producto(); producto.setNombre("Prod API"); producto.setSlug("prod-api-"+System.currentTimeMillis()); producto = productoRepository.save(producto);
        Variante variante = new Variante(); variante.setProducto(producto); variante.setSku("SKU-API-"+System.currentTimeMillis()); variante.setEsDefault(true); variante.setActivo(true); variante = varianteRepository.save(variante);

        Map<String,Object> payload = new HashMap<>();
        payload.put("weightGrams", 500);
        payload.put("widthMm", 200);
        payload.put("heightMm", 100);
        payload.put("depthMm", 50);
        payload.put("packageWeightGrams", 600);
        payload.put("packageWidthMm", 220);
        payload.put("packageHeightMm", 120);
        payload.put("packageDepthMm", 70);

        var result = mockMvc.perform(MockMvcRequestBuilders.put("/dev/api/variantes/{varianteId}/physical", variante.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        var dto = objectMapper.readValue(result.getResponse().getContentAsString(), Map.class);
        assertEquals(200, ((Number)dto.get("widthMm")).intValue());
    }

    @Test
    void get_ObtenerVarianteFisico_Status200_API() throws Exception {
        Producto producto = new Producto(); producto.setNombre("Prod API"); producto.setSlug("prod-api-"+System.currentTimeMillis()); producto = productoRepository.save(producto);
        Variante variante = new Variante(); variante.setProducto(producto); variante.setSku("SKU-API-"+System.currentTimeMillis()); variante.setEsDefault(true); variante.setActivo(true); variante = varianteRepository.save(variante);
        VarianteFisico fisico = new VarianteFisico(); fisico.setVariante(variante); fisico.setWidthMm(111); fisico.setHeightMm(222);
        fisico.setDepthMm(10); fisico.setWeightGrams(150);
        fisico.setPackageWidthMm(131); fisico.setPackageHeightMm(242); fisico.setPackageDepthMm(20); fisico.setPackageWeightGrams(170);
        fisico = repository.save(fisico);

        var result = mockMvc.perform(MockMvcRequestBuilders.get("/dev/api/variantes/{varianteId}/physical", variante.getId())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        var dto = objectMapper.readValue(result.getResponse().getContentAsString(), Map.class);
        assertEquals(111, ((Number)dto.get("widthMm")).intValue());
    }

    @Test
    void delete_DeberiaSoftDeleteVarianteFisico_Status204_API() throws Exception {
        Producto producto = new Producto(); producto.setNombre("Prod API"); producto.setSlug("prod-api-"+System.currentTimeMillis()); producto = productoRepository.save(producto);
        Variante variante = new Variante(); variante.setProducto(producto); variante.setSku("SKU-API-"+System.currentTimeMillis()); variante.setEsDefault(true); variante.setActivo(true); variante = varianteRepository.save(variante);
        VarianteFisico fisico = new VarianteFisico(); fisico.setVariante(variante); fisico.setWidthMm(50); fisico.setHeightMm(5); fisico.setDepthMm(2); fisico.setWeightGrams(60);
        fisico.setPackageWidthMm(70); fisico.setPackageHeightMm(25); fisico.setPackageDepthMm(5); fisico.setPackageWeightGrams(80);
        fisico = repository.save(fisico);

        // asegurar que los cambios se sincronizan con la DB antes de la petición HTTP
        //entityManager.flush();
        //entityManager.clear();
        assertTrue(repository.findByVariante_Id(variante.getId()).isPresent());

        mockMvc.perform(MockMvcRequestBuilders.delete("/dev/api/variantes/{varianteId}/physical", variante.getId()))
            .andExpect(MockMvcResultMatchers.status().isNoContent());

        var refreshed = repository.findByVariante_Id(variante.getId());
        assertTrue(refreshed.isPresent());
        assertNotNull(refreshed.get().getDeletedAt());
    }

    @Test
    void delete_DeberiaEliminarFisicoVarianteFisico_Status204_API() throws Exception {
        Producto producto = new Producto(); producto.setNombre("Prod API"); producto.setSlug("prod-api-"+System.currentTimeMillis()); producto = productoRepository.save(producto);
        Variante variante = new Variante(); variante.setProducto(producto); variante.setSku("SKU-API-"+System.currentTimeMillis()); variante.setEsDefault(true); variante.setActivo(true); variante = varianteRepository.save(variante);
        VarianteFisico fisico = new VarianteFisico(); fisico.setVariante(variante); fisico.setWidthMm(55); fisico.setHeightMm(6); fisico.setDepthMm(3); fisico.setWeightGrams(70);
        fisico.setPackageWidthMm(75); fisico.setPackageHeightMm(26); fisico.setPackageDepthMm(6); fisico.setPackageWeightGrams(90);
        fisico = repository.save(fisico);

        // asegurar que los cambios se sincronizan con la DB antes de la petición HTTP
        //entityManager.flush();
        //entityManager.clear();
        assertTrue(repository.findByVariante_Id(variante.getId()).isPresent());

        mockMvc.perform(MockMvcRequestBuilders.delete("/dev/api/variantes/{varianteId}/physical/hard", variante.getId()))
            .andExpect(MockMvcResultMatchers.status().isNoContent());

        var refreshed = repository.findByVariante_Id(variante.getId());
        assertTrue(refreshed.isEmpty());
    }

    // === VERIFICACIONES DB REALES ===

    @Order(1)
    @Test
    @Transactional
    void db_Transaccion_Rollback_Crear_NoPersiste() {
        Producto producto = new Producto(); producto.setNombre("RB Prod VF"); producto.setSlug("rb-prod-vf-"+System.currentTimeMillis()); producto.setDescripcion("rb"); productoRepository.save(producto);
        Variante variante = new Variante(); variante.setProducto(producto); variante.setSku("rb-sku-vf-"+System.currentTimeMillis()); variante.setEsDefault(true); variante.setActivo(true); varianteRepository.save(variante);

        VarianteFisico vf = new VarianteFisico(); vf.setVariante(variante); vf.setWidthMm(10); vf.setHeightMm(5); vf.setDepthMm(2); vf.setWeightGrams(100);
        vf.setPackageWidthMm(12); vf.setPackageHeightMm(6); vf.setPackageDepthMm(4); vf.setPackageWeightGrams(120);
        repository.save(vf);
        entityManager.flush();
    }

    @Order(2)
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void db_Transaccion_Rollback_Verificar_NoExiste() {
        Object cnt = entityManager.createNativeQuery("SELECT count(*) FROM variante_fisico vf JOIN variante v ON vf.variante_id = v.id WHERE v.sku LIKE :sku")
                .setParameter("sku", "%rb-sku-vf-%")
                .getSingleResult();
        long count = cnt==null?0L:((Number)cnt).longValue();
        assertEquals(0L, count, "La entidad creada en la transacción anterior debe haber sido rollback-eada");
    }
    
    // REVIEW: verificar el test, no esta funcionando, llave duplicada, no se hace rollback, etc. Posible causa: el test de creación no lanza excepción y por eso no hace rollback, o el flush no sincroniza correctamente. Alternativamente, revisar si el método de limpieza @AfterEach funciona correctamente para eliminar los datos de prueba.
    /* 
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void verificar_Tablas_Vacias_Precondicion() {
        String[] tables = new String[]{
                "variante_precio",
                "variante_imagen",
                "variante_opcion_valor",
                "variante_opcion",
                "variante_valor",
                "variante_fisico",
                "variante",
                "producto"
        };
        for (String t : tables) {
            Object cnt = entityManager.createNativeQuery("SELECT count(*) FROM " + t).getSingleResult();
            long c = cnt == null ? 0L : ((Number) cnt).longValue();
            assertEquals(0L, c, "Precondición: la tabla '" + t + "' debe estar vacía antes del test");
        }
    }

    @Order(3)
    @Test
    @Transactional
    @Rollback
    void db_Constraint_UniqueYNotNull_Y_Audit() {
        // Paso 1: crear producto y variante (NO-default para evitar validaciones de negocio)
        Producto producto = new Producto(); producto.setNombre("P VF"); producto.setSlug("p-vf-"+System.currentTimeMillis()); productoRepository.save(producto);
        Variante variante = new Variante(); variante.setProducto(producto); variante.setEsDefault(false); variante.setActivo(true); variante.setSku("sku-vf-"+UUID.randomUUID());
        Variante savedVariante = varianteRepository.save(variante);
        Long vid = savedVariante.getId();
        assertNotNull(vid, "Paso 1: variante creada");

        // Paso 2: asegurar inexistencia previa para esta variante (delete directo y comprobación)
        int before = ((Number) entityManager.createNativeQuery("SELECT count(*) FROM variante_fisico WHERE variante_id = :vid").setParameter("vid", vid).getSingleResult()).intValue();
        if (before != 0) {
            entityManager.createNativeQuery("DELETE FROM variante_fisico WHERE variante_id = :vid").setParameter("vid", vid).executeUpdate();
            // forzar sincronización
            entityManager.createNativeQuery("SELECT 1").getSingleResult();
        }
        int after = ((Number) entityManager.createNativeQuery("SELECT count(*) FROM variante_fisico WHERE variante_id = :vid").setParameter("vid", vid).getSingleResult()).intValue();
        assertEquals(0, after, "Paso 2: no deben existir registros previos para variante id=" + vid);

        // Paso 3: insertar el primer VarianteFisico
        VarianteFisico first = new VarianteFisico();
        first.setVariante(savedVariante); first.setWidthMm(50); first.setHeightMm(20); first.setDepthMm(10); first.setWeightGrams(300);
        first = repository.saveAndFlush(first);
        assertNotNull(first.getId(), "Paso 3: primer VarianteFisico insertado");

        // Paso 4: detectar si la BD tiene constraint única sobre variante_id
        boolean uniqueOnVarianteId = false;
        try {
            Object q = entityManager.createNativeQuery(
                    "SELECT 1 FROM pg_constraint c JOIN pg_class t ON c.conrelid = t.oid JOIN pg_attribute a ON a.attrelid = t.oid AND a.attnum = ANY(c.conkey) WHERE t.relname='variante_fisico' AND c.contype='u' AND a.attname='variante_id' LIMIT 1"
            ).getSingleResult();
            uniqueOnVarianteId = q != null;
        } catch (Exception ex) {
            uniqueOnVarianteId = false;
        }
        System.out.println("Paso 4: uniqueOnVarianteId=" + uniqueOnVarianteId);

        // Paso 5: intentar insertar duplicado y comprobar comportamiento
        VarianteFisico dup = new VarianteFisico(); dup.setVariante(savedVariante); dup.setWidthMm(50); dup.setHeightMm(20); dup.setDepthMm(10); dup.setWeightGrams(300);
        if (uniqueOnVarianteId) {
            try {
                TransactionTemplate tt = new TransactionTemplate(transactionManager);
                tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                tt.execute(status -> {
                    repository.saveAndFlush(dup);
                    return null;
                });
                fail("Paso 5: esperaba excepción por constraint única, pero no se lanzó");
            } catch (Exception ex) {
                System.out.println("Paso 5: excepción esperada al insertar duplicado: " + ex.getClass().getSimpleName());
            }
        } else {
            repository.saveAndFlush(dup);
            long cnt = repository.findAll().stream().filter(x -> x.getVariante().getId().equals(vid) && x.getWidthMm() == 50).count();
            assertTrue(cnt >= 2, "Paso 5: si no hay constraint, deberían existir al menos 2 registros similares");
        }

        // Paso 6: verificar nullable/not-null de la columna width_mm
        boolean widthNotNull = false;
        try{
            Object nn = entityManager.createNativeQuery("SELECT is_nullable FROM information_schema.columns WHERE table_name='variante_fisico' AND column_name='width_mm'").getSingleResult();
            if (nn!=null) widthNotNull = "NO".equalsIgnoreCase(nn.toString());
        } catch(Exception ex){ widthNotNull=false; }
        VarianteFisico c = new VarianteFisico(); c.setVariante(savedVariante); c.setWidthMm(null); c.setHeightMm(1);
        if (widthNotNull) {
            try {
                TransactionTemplate tt2 = new TransactionTemplate(transactionManager);
                tt2.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                tt2.execute(status -> {
                    repository.saveAndFlush(c);
                    return null;
                });
                fail("Paso 6: esperaba DataIntegrityViolationException al insertar null en width_mm");
            } catch (DataIntegrityViolationException ex) {
                System.out.println("Paso 6: DataIntegrityViolationException esperada (width_mm NOT NULL)");
            } catch (Exception ex) {
                System.out.println("Paso 6: excepción esperada al insertar null en width_mm: " + ex.getClass().getSimpleName());
            }
        } else {
            repository.saveAndFlush(c);
            var saved = repository.findById(c.getId()).orElseThrow();
            assertNull(saved.getWidthMm(), "Paso 6: width_mm se guardó como NULL");
        }

        // Paso 7: verificar audit (createdAt) sobre el primer registro creado
        var savedFirst = repository.findById(first.getId()).orElseThrow();
        assertNotNull(savedFirst.getCreatedAt(), "Paso 7: createdAt debe estar seteado para el primer registro");
    }
    */
}
