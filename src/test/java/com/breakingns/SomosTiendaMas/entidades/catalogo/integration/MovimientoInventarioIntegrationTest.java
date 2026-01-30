package com.breakingns.SomosTiendaMas.entidades.catalogo.integration;

import com.breakingns.SomosTiendaMas.entidades.catalogo.enums.TipoMovimientoInventario;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.MovimientoInventario;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Producto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Variante;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.MovimientoInventarioRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.ProductoRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.VarianteRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
public class MovimientoInventarioIntegrationTest {
    
    @Autowired
    private MovimientoInventarioRepository repository;
    
    @Autowired
    private VarianteRepository varianteRepository;
    
    @Autowired
    private ProductoRepository productoRepository;
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @BeforeEach
    void setUp() {
        entityManager.createNativeQuery("SELECT setval('movimiento_inventario_id_seq', (SELECT COALESCE(MAX(id), 1) FROM movimiento_inventario))")
                .getSingleResult();
    }
    
    @Test
    void save_DeberiaCrearMovimientoInventario_CuandoDatosValidos() {
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
        
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setVariante(variante);
        movimiento.setTipo(TipoMovimientoInventario.ENTRADA_AJUSTE);
        movimiento.setCantidad(50L);
        movimiento.setOrderRef("ORDER-" + System.currentTimeMillis());
        movimiento.setReferenciaId("REF-123");
        
        // When
        MovimientoInventario resultado = repository.save(movimiento);
        
        // Then
        assertNotNull(resultado.getId());
        assertEquals(TipoMovimientoInventario.ENTRADA_AJUSTE, resultado.getTipo());
        assertEquals(50L, resultado.getCantidad());
        assertNotNull(resultado.getOrderRef());
        assertEquals("REF-123", resultado.getReferenciaId());
    }
    
    @Test
    void findById_DeberiaRetornarMovimientoInventario_CuandoExiste() {
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
        
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setVariante(variante);
        movimiento.setTipo(TipoMovimientoInventario.SALIDA_VENTA);
        movimiento.setCantidad(20L);
        movimiento.setOrderRef("ORDER-" + System.currentTimeMillis());
        movimiento = repository.save(movimiento);
        
        // When
        Optional<MovimientoInventario> resultado = repository.findById(movimiento.getId());
        
        // Then
        assertTrue(resultado.isPresent());
        assertEquals(TipoMovimientoInventario.SALIDA_VENTA, resultado.get().getTipo());
        assertEquals(20L, resultado.get().getCantidad());
    }
    
    @Test
    void findAll_DeberiaRetornarLista_CuandoExistenMovimientosInventario() {
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
        
        MovimientoInventario movimiento1 = new MovimientoInventario();
        movimiento1.setVariante(variante1);
        movimiento1.setTipo(TipoMovimientoInventario.ENTRADA_AJUSTE);
        movimiento1.setCantidad(100L);
        movimiento1.setOrderRef("ORDER-1-" + System.currentTimeMillis());
        repository.save(movimiento1);
        
        MovimientoInventario movimiento2 = new MovimientoInventario();
        movimiento2.setVariante(variante1);
        movimiento2.setTipo(TipoMovimientoInventario.SALIDA_VENTA);
        movimiento2.setCantidad(30L);
        movimiento2.setOrderRef("ORDER-2-" + System.currentTimeMillis());
        repository.save(movimiento2);
        
        MovimientoInventario movimiento3 = new MovimientoInventario();
        movimiento3.setVariante(variante2);
        movimiento3.setTipo(TipoMovimientoInventario.RESERVA);
        movimiento3.setCantidad(5L);
        movimiento3.setOrderRef("ORDER-3-" + System.currentTimeMillis());
        repository.save(movimiento3);
        
        // When
        List<MovimientoInventario> resultados = repository.findAll();
        
        // Then
        assertTrue(resultados.size() >= 3);
    }
    
    @Test
    void delete_DeberiaEliminarMovimientoInventario_CuandoExiste() {
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
        
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setVariante(variante);
        movimiento.setTipo(TipoMovimientoInventario.ENTRADA_AJUSTE);
        movimiento.setCantidad(50L);
        movimiento.setOrderRef("ORDER-" + System.currentTimeMillis());
        movimiento = repository.save(movimiento);
        
        Long id = movimiento.getId();
        
        // When
        repository.deleteById(id);
        
        // Then
        Optional<MovimientoInventario> resultado = repository.findById(id);
        assertFalse(resultado.isPresent());
    }
    
    @Test
    void update_DeberiaActualizarMovimientoInventario_CuandoExiste() {
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
        
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setVariante(variante);
        movimiento.setTipo(TipoMovimientoInventario.ENTRADA_AJUSTE);
        movimiento.setCantidad(50L);
        movimiento.setOrderRef("ORDER-" + System.currentTimeMillis());
        movimiento.setReferenciaId("REF-ORIGINAL");
        movimiento = repository.save(movimiento);
        
        // When
        movimiento.setCantidad(75L);
        movimiento.setReferenciaId("REF-ACTUALIZADA");
        movimiento.setMetadataJson("{\"updated\": true}");
        MovimientoInventario actualizado = repository.save(movimiento);
        
        // Then
        Optional<MovimientoInventario> resultado = repository.findById(actualizado.getId());
        assertTrue(resultado.isPresent());
        assertEquals(75L, resultado.get().getCantidad());
        assertEquals("REF-ACTUALIZADA", resultado.get().getReferenciaId());
        assertEquals("{\"updated\": true}", resultado.get().getMetadataJson());
    }
    
}
