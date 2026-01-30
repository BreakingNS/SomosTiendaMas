package com.breakingns.SomosTiendaMas.entidades.catalogo.integration;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Producto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Variante;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.VarianteFisico;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.ProductoRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.VarianteFisicoRepository;
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
public class VarianteFisicoIntegrationTest {
    
    @Autowired
    private VarianteFisicoRepository repository;
    
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
        entityManager.createNativeQuery("SELECT setval('variante_fisico_id_seq', (SELECT COALESCE(MAX(id), 1) FROM variante_fisico))")
                .getSingleResult();
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
        fisico = repository.save(fisico);
        
        Long id = fisico.getId();
        
        // When
        repository.deleteById(id);
        
        // Then
        Optional<VarianteFisico> resultado = repository.findById(id);
        assertFalse(resultado.isPresent());
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
    
}
