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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
public class ImagenVarianteIntegrationTest {
    
    @Autowired
    private ImagenVarianteRepository repository;
    
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
        
        // When
        repository.deleteById(id);
        
        // Then
        Optional<ImagenVariante> resultado = repository.findById(id);
        assertFalse(resultado.isPresent());
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
    
}
