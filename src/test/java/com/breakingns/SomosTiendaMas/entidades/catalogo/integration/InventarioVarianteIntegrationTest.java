package com.breakingns.SomosTiendaMas.entidades.catalogo.integration;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.InventarioVariante;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Producto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Variante;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.InventarioVarianteRepository;
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
public class InventarioVarianteIntegrationTest {
    
    @Autowired
    private InventarioVarianteRepository repository;
    
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
        entityManager.createNativeQuery("SELECT setval('variante_inventario_id_seq', (SELECT COALESCE(MAX(id), 1) FROM variante_inventario))")
                .getSingleResult();
    }
    
    @Test
    void save_DeberiaCrearInventarioVariante_CuandoDatosValidos() {
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
        
        InventarioVariante inventario = new InventarioVariante();
        inventario.setVariante(variante);
        inventario.setOnHand(100);
        inventario.setReserved(10);
        inventario.setUbicacion("Estantería A1");
        
        // When
        InventarioVariante resultado = repository.save(inventario);
        
        // Then
        assertNotNull(resultado.getId());
        assertEquals(100, resultado.getOnHand());
        assertEquals(10, resultado.getReserved());
        assertEquals("Estantería A1", resultado.getUbicacion());
        assertNotNull(resultado.getVersion());
    }
    
    @Test
    void findById_DeberiaRetornarInventarioVariante_CuandoExiste() {
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
        
        InventarioVariante inventario = new InventarioVariante();
        inventario.setVariante(variante);
        inventario.setOnHand(50);
        inventario.setReserved(5);
        inventario = repository.save(inventario);
        
        // When
        Optional<InventarioVariante> resultado = repository.findById(inventario.getId());
        
        // Then
        assertTrue(resultado.isPresent());
        assertEquals(50, resultado.get().getOnHand());
        assertEquals(5, resultado.get().getReserved());
    }
    
    @Test
    void findAll_DeberiaRetornarLista_CuandoExistenInventariosVariante() {
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
        
        InventarioVariante inventario1 = new InventarioVariante();
        inventario1.setVariante(variante1);
        inventario1.setOnHand(100);
        inventario1.setReserved(10);
        repository.save(inventario1);
        
        InventarioVariante inventario2 = new InventarioVariante();
        inventario2.setVariante(variante2);
        inventario2.setOnHand(50);
        inventario2.setReserved(5);
        repository.save(inventario2);
        
        InventarioVariante inventario3 = new InventarioVariante();
        inventario3.setVariante(variante3);
        inventario3.setOnHand(0);
        inventario3.setReserved(0);
        repository.save(inventario3);
        
        // When
        List<InventarioVariante> resultados = repository.findAll();
        
        // Then
        assertTrue(resultados.size() >= 3);
    }
    
    @Test
    void delete_DeberiaEliminarInventarioVariante_CuandoExiste() {
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
        
        InventarioVariante inventario = new InventarioVariante();
        inventario.setVariante(variante);
        inventario.setOnHand(100);
        inventario.setReserved(0);
        inventario = repository.save(inventario);
        
        Long id = inventario.getId();
        
        // When
        repository.deleteById(id);
        
        // Then
        Optional<InventarioVariante> resultado = repository.findById(id);
        assertFalse(resultado.isPresent());
    }
    
    @Test
    void update_DeberiaActualizarInventarioVariante_CuandoExiste() {
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
        
        InventarioVariante inventario = new InventarioVariante();
        inventario.setVariante(variante);
        inventario.setOnHand(100);
        inventario.setReserved(10);
        inventario.setUbicacion("Ubicación Original");
        inventario = repository.save(inventario);
        
        // When
        inventario.setOnHand(150);
        inventario.setReserved(20);
        inventario.setUbicacion("Ubicación Actualizada");
        InventarioVariante actualizado = repository.save(inventario);
        
        // Then
        Optional<InventarioVariante> resultado = repository.findById(actualizado.getId());
        assertTrue(resultado.isPresent());
        assertEquals(150, resultado.get().getOnHand());
        assertEquals(20, resultado.get().getReserved());
        assertEquals("Ubicación Actualizada", resultado.get().getUbicacion());
    }
    
}
