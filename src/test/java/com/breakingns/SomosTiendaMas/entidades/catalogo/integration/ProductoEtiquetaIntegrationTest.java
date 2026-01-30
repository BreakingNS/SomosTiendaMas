package com.breakingns.SomosTiendaMas.entidades.catalogo.integration;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Producto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Etiqueta;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.ProductoEtiqueta;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.ProductoRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.EtiquetaRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.ProductoEtiquetaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
public class ProductoEtiquetaIntegrationTest {
    
    @Autowired
    private ProductoEtiquetaRepository repository;
    
    @Autowired
    private ProductoRepository productoRepository;
    
    @Autowired
    private EtiquetaRepository etiquetaRepository;
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @BeforeEach
    void sincronizarSecuencia() {
        entityManager.createNativeQuery("SELECT setval('producto_etiqueta_id_seq', (SELECT COALESCE(MAX(id), 1) FROM producto_etiqueta))")
                     .getSingleResult();
    }
    
    // === TESTS DE REPOSITORY ===
    
    @Test
    void save_DeberiaCrearProductoEtiqueta_CuandoDatosValidos() {
        // Crear producto padre
        Producto producto = new Producto();
        producto.setNombre("Producto Test");
        producto.setSlug("producto-test-" + System.currentTimeMillis());
        producto = productoRepository.save(producto);
        
        // Crear etiqueta padre
        Etiqueta etiqueta = new Etiqueta();
        etiqueta.setNombre("Oferta");
        etiqueta.setSlug("oferta-test");
        etiqueta = etiquetaRepository.save(etiqueta);
        
        // Crear entidad con datos válidos
        ProductoEtiqueta productoEtiqueta = new ProductoEtiqueta();
        productoEtiqueta.setProducto(producto);
        productoEtiqueta.setEtiqueta(etiqueta);
        
        // Guardar en DB
        ProductoEtiqueta guardada = repository.save(productoEtiqueta);
        
        // Verificar que ID fue generado
        assertNotNull(guardada.getId(), "El ID debe ser generado automáticamente");
        
        // Verificar que los datos se guardaron correctamente
        assertNotNull(guardada.getProducto());
        assertNotNull(guardada.getEtiqueta());
        assertEquals(producto.getId(), guardada.getProducto().getId());
        assertEquals(etiqueta.getId(), guardada.getEtiqueta().getId());
    }
    
    @Test
    void findById_DeberiaRetornarProductoEtiqueta_CuandoExiste() {
        // Crear producto padre
        Producto producto = new Producto();
        producto.setNombre("Producto Test 2");
        producto.setSlug("producto-test-2-" + System.currentTimeMillis());
        producto = productoRepository.save(producto);
        
        // Crear etiqueta padre
        Etiqueta etiqueta = new Etiqueta();
        etiqueta.setNombre("Destacado");
        etiqueta.setSlug("destacado-test");
        etiqueta = etiquetaRepository.save(etiqueta);
        
        // Guardar entidad en DB
        ProductoEtiqueta productoEtiqueta = new ProductoEtiqueta();
        productoEtiqueta.setProducto(producto);
        productoEtiqueta.setEtiqueta(etiqueta);
        ProductoEtiqueta guardada = repository.save(productoEtiqueta);
        
        // Buscar por ID
        var resultado = repository.findById(guardada.getId());
        
        // Verificar que Optional.isPresent() es true
        assertTrue(resultado.isPresent(), "Debe encontrar el producto etiqueta por ID");
        
        // Verificar datos coinciden
        ProductoEtiqueta encontrada = resultado.get();
        assertEquals(producto.getId(), encontrada.getProducto().getId());
        assertEquals(etiqueta.getId(), encontrada.getEtiqueta().getId());
    }
    
    @Test
    void findAll_DeberiaRetornarLista_CuandoExistenProductoEtiquetas() {
        // Crear producto padre
        Producto producto = new Producto();
        producto.setNombre("Producto Test 3");
        producto.setSlug("producto-test-3-" + System.currentTimeMillis());
        producto = productoRepository.save(producto);
        
        // Guardar 3 entidades diferentes con distintas etiquetas
        Etiqueta etiqueta1 = new Etiqueta();
        etiqueta1.setNombre("Etiqueta 1");
        etiqueta1.setSlug("etiqueta-1-test");
        etiqueta1 = etiquetaRepository.save(etiqueta1);
        
        ProductoEtiqueta productoEtiqueta1 = new ProductoEtiqueta();
        productoEtiqueta1.setProducto(producto);
        productoEtiqueta1.setEtiqueta(etiqueta1);
        repository.save(productoEtiqueta1);
        
        Etiqueta etiqueta2 = new Etiqueta();
        etiqueta2.setNombre("Etiqueta 2");
        etiqueta2.setSlug("etiqueta-2-test");
        etiqueta2 = etiquetaRepository.save(etiqueta2);
        
        ProductoEtiqueta productoEtiqueta2 = new ProductoEtiqueta();
        productoEtiqueta2.setProducto(producto);
        productoEtiqueta2.setEtiqueta(etiqueta2);
        repository.save(productoEtiqueta2);
        
        Etiqueta etiqueta3 = new Etiqueta();
        etiqueta3.setNombre("Etiqueta 3");
        etiqueta3.setSlug("etiqueta-3-test");
        etiqueta3 = etiquetaRepository.save(etiqueta3);
        
        ProductoEtiqueta productoEtiqueta3 = new ProductoEtiqueta();
        productoEtiqueta3.setProducto(producto);
        productoEtiqueta3.setEtiqueta(etiqueta3);
        repository.save(productoEtiqueta3);
        
        // Llamar findAll()
        var lista = repository.findAll();
        
        // Verificar que retorna 3+ elementos (puede haber datos previos)
        assertTrue(lista.size() >= 3, "Debe retornar al menos 3 producto etiquetas");
    }
    
    @Test
    void delete_DeberiaEliminarProductoEtiqueta_CuandoExiste() {
        // Crear producto padre
        Producto producto = new Producto();
        producto.setNombre("Producto Test 4");
        producto.setSlug("producto-test-4-" + System.currentTimeMillis());
        producto = productoRepository.save(producto);
        
        // Crear etiqueta padre
        Etiqueta etiqueta = new Etiqueta();
        etiqueta.setNombre("ParaBorrar");
        etiqueta.setSlug("para-borrar-test");
        etiqueta = etiquetaRepository.save(etiqueta);
        
        // Guardar entidad
        ProductoEtiqueta productoEtiqueta = new ProductoEtiqueta();
        productoEtiqueta.setProducto(producto);
        productoEtiqueta.setEtiqueta(etiqueta);
        ProductoEtiqueta guardada = repository.save(productoEtiqueta);
        Long id = guardada.getId();
        
        // Llamar deleteById
        repository.deleteById(id);
        
        // Verificar que repository.findById(id).isEmpty() es true
        assertTrue(repository.findById(id).isEmpty(), "El producto etiqueta debe haber sido eliminado");
    }
    
    @Test
    void update_DeberiaActualizarProductoEtiqueta_CuandoExiste() {
        // Crear producto padre
        Producto producto = new Producto();
        producto.setNombre("Producto Test 5");
        producto.setSlug("producto-test-5-" + System.currentTimeMillis());
        producto = productoRepository.save(producto);
        
        // Crear etiqueta original
        Etiqueta etiquetaOriginal = new Etiqueta();
        etiquetaOriginal.setNombre("Original");
        etiquetaOriginal.setSlug("original-test");
        etiquetaOriginal = etiquetaRepository.save(etiquetaOriginal);
        
        // Guardar entidad con etiqueta "Original"
        ProductoEtiqueta productoEtiqueta = new ProductoEtiqueta();
        productoEtiqueta.setProducto(producto);
        productoEtiqueta.setEtiqueta(etiquetaOriginal);
        ProductoEtiqueta guardada = repository.save(productoEtiqueta);
        
        // Crear nueva etiqueta "Actualizada"
        Etiqueta etiquetaActualizada = new Etiqueta();
        etiquetaActualizada.setNombre("Actualizado");
        etiquetaActualizada.setSlug("actualizado-test");
        etiquetaActualizada = etiquetaRepository.save(etiquetaActualizada);
        
        // Modificar etiqueta a "Actualizado"
        guardada.setEtiqueta(etiquetaActualizada);
        
        // Llamar repository.save(entidadModificada)
        repository.save(guardada);
        
        // Buscar por ID y verificar etiqueta es "Actualizado"
        var resultado = repository.findById(guardada.getId());
        assertTrue(resultado.isPresent());
        assertEquals("Actualizado", resultado.get().getEtiqueta().getNombre());
        assertEquals(etiquetaActualizada.getId(), resultado.get().getEtiqueta().getId());
    }
    
}
