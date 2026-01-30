package com.breakingns.SomosTiendaMas.entidades.catalogo.integration;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.*;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.*;
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
public class VarianteValorIntegrationTest {
    
    @Autowired
    private VarianteValorRepository repository;
    
    @Autowired
    private VarianteRepository varianteRepository;
    
    @Autowired
    private ProductoRepository productoRepository;
    
    @Autowired
    private OpcionValorRepository opcionValorRepository;
    
    @Autowired
    private OpcionRepository opcionRepository;
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @BeforeEach
    void setUp() {
        entityManager.createNativeQuery("SELECT setval('variante_valor_id_seq', (SELECT COALESCE(MAX(id), 1) FROM variante_valor))")
                .getSingleResult();
    }
    
    @Test
    void save_DeberiaCrearVarianteValor_CuandoDatosValidos() {
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
        
        Opcion opcion = new Opcion();
        opcion.setNombre("Opción Test");
        opcion.setOrden(1);
        opcion.setTipo("SIMPLE");
        opcion = opcionRepository.save(opcion);
        
        OpcionValor opcionValor = new OpcionValor();
        opcionValor.setOpcion(opcion);
        opcionValor.setValor("Valor Test");
        opcionValor.setSlug("slug-" + System.currentTimeMillis());
        opcionValor.setOrden(1);
        opcionValor = opcionValorRepository.save(opcionValor);
        
        VarianteValor varianteValor = new VarianteValor();
        varianteValor.setVariante(variante);
        varianteValor.setValor(opcionValor);
        
        // When
        VarianteValor resultado = repository.save(varianteValor);
        
        // Then
        assertNotNull(resultado.getId());
        assertEquals(variante.getId(), resultado.getVariante().getId());
        assertEquals(opcionValor.getId(), resultado.getValor().getId());
    }
    
    @Test
    void findById_DeberiaRetornarVarianteValor_CuandoExiste() {
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
        
        Opcion opcion = new Opcion();
        opcion.setNombre("Opción Test");
        opcion.setOrden(1);
        opcion.setTipo("SIMPLE");
        opcion = opcionRepository.save(opcion);
        
        OpcionValor opcionValor = new OpcionValor();
        opcionValor.setOpcion(opcion);
        opcionValor.setValor("Valor Test");
        opcionValor.setSlug("slug-" + System.currentTimeMillis());
        opcionValor.setOrden(1);
        opcionValor = opcionValorRepository.save(opcionValor);
        
        VarianteValor varianteValor = new VarianteValor();
        varianteValor.setVariante(variante);
        varianteValor.setValor(opcionValor);
        varianteValor = repository.save(varianteValor);
        
        // When
        Optional<VarianteValor> resultado = repository.findById(varianteValor.getId());
        
        // Then
        assertTrue(resultado.isPresent());
        assertEquals(variante.getId(), resultado.get().getVariante().getId());
        assertEquals(opcionValor.getId(), resultado.get().getValor().getId());
    }
    
    @Test
    void findAll_DeberiaRetornarLista_CuandoExistenVarianteValores() {
        // Given
        Producto producto1 = new Producto();
        producto1.setNombre("Producto 1");
        producto1.setSlug("slug-1-" + System.currentTimeMillis());
        producto1.setDescripcion("Descripción 1");
        producto1 = productoRepository.save(producto1);
        
        Producto producto2 = new Producto();
        producto2.setNombre("Producto 2");
        producto2.setSlug("slug-2-" + System.currentTimeMillis());
        producto2.setDescripcion("Descripción 2");
        producto2 = productoRepository.save(producto2);
        
        Variante variante1 = new Variante();
        variante1.setProducto(producto1);
        variante1.setSku("SKU-1-" + System.currentTimeMillis());
        variante1.setEsDefault(true);
        variante1.setActivo(true);
        variante1 = varianteRepository.save(variante1);
        
        Variante variante2 = new Variante();
        variante2.setProducto(producto2);
        variante2.setSku("SKU-2-" + System.currentTimeMillis());
        variante2.setEsDefault(true);
        variante2.setActivo(true);
        variante2 = varianteRepository.save(variante2);
        
        Opcion opcion1 = new Opcion();
        opcion1.setNombre("Opción 1");
        opcion1.setOrden(1);
        opcion1.setTipo("SIMPLE");
        opcion1 = opcionRepository.save(opcion1);
        
        Opcion opcion2 = new Opcion();
        opcion2.setNombre("Opción 2");
        opcion2.setOrden(2);
        opcion2.setTipo("SIMPLE");
        opcion2 = opcionRepository.save(opcion2);
        
        OpcionValor opcionValor1 = new OpcionValor();
        opcionValor1.setOpcion(opcion1);
        opcionValor1.setValor("Valor 1");
        opcionValor1.setSlug("slug-val-1-" + System.currentTimeMillis());
        opcionValor1.setOrden(1);
        opcionValor1 = opcionValorRepository.save(opcionValor1);
        
        OpcionValor opcionValor2 = new OpcionValor();
        opcionValor2.setOpcion(opcion2);
        opcionValor2.setValor("Valor 2");
        opcionValor2.setSlug("slug-val-2-" + System.currentTimeMillis());
        opcionValor2.setOrden(1);
        opcionValor2 = opcionValorRepository.save(opcionValor2);
        
        VarianteValor varianteValor1 = new VarianteValor();
        varianteValor1.setVariante(variante1);
        varianteValor1.setValor(opcionValor1);
        repository.save(varianteValor1);
        
        VarianteValor varianteValor2 = new VarianteValor();
        varianteValor2.setVariante(variante2);
        varianteValor2.setValor(opcionValor2);
        repository.save(varianteValor2);
        
        // When
        List<VarianteValor> resultados = repository.findAll();
        
        // Then
        assertTrue(resultados.size() >= 2);
    }
    
    @Test
    void delete_DeberiaEliminarVarianteValor_CuandoExiste() {
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
        
        Opcion opcion = new Opcion();
        opcion.setNombre("Opción Test");
        opcion.setOrden(1);
        opcion.setTipo("SIMPLE");
        opcion = opcionRepository.save(opcion);
        
        OpcionValor opcionValor = new OpcionValor();
        opcionValor.setOpcion(opcion);
        opcionValor.setValor("Valor Test");
        opcionValor.setSlug("slug-" + System.currentTimeMillis());
        opcionValor.setOrden(1);
        opcionValor = opcionValorRepository.save(opcionValor);
        
        VarianteValor varianteValor = new VarianteValor();
        varianteValor.setVariante(variante);
        varianteValor.setValor(opcionValor);
        varianteValor = repository.save(varianteValor);
        
        Long id = varianteValor.getId();
        
        // When
        repository.deleteById(id);
        
        // Then
        Optional<VarianteValor> resultado = repository.findById(id);
        assertFalse(resultado.isPresent());
    }
    
    @Test
    void update_DeberiaActualizarVarianteValor_CuandoExiste() {
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
        
        Opcion opcion = new Opcion();
        opcion.setNombre("Opción Test");
        opcion.setOrden(1);
        opcion.setTipo("SIMPLE");
        opcion = opcionRepository.save(opcion);
        
        OpcionValor opcionValor1 = new OpcionValor();
        opcionValor1.setOpcion(opcion);
        opcionValor1.setValor("Valor Original");
        opcionValor1.setSlug("slug-orig-" + System.currentTimeMillis());
        opcionValor1.setOrden(1);
        opcionValor1 = opcionValorRepository.save(opcionValor1);
        
        OpcionValor opcionValor2 = new OpcionValor();
        opcionValor2.setOpcion(opcion);
        opcionValor2.setValor("Valor Actualizado");
        opcionValor2.setSlug("slug-act-" + System.currentTimeMillis());
        opcionValor2.setOrden(2);
        opcionValor2 = opcionValorRepository.save(opcionValor2);
        
        VarianteValor varianteValor = new VarianteValor();
        varianteValor.setVariante(variante);
        varianteValor.setValor(opcionValor1);
        varianteValor = repository.save(varianteValor);
        
        // When
        varianteValor.setValor(opcionValor2);
        VarianteValor actualizado = repository.save(varianteValor);
        
        // Then
        Optional<VarianteValor> resultado = repository.findById(actualizado.getId());
        assertTrue(resultado.isPresent());
        assertEquals(opcionValor2.getId(), resultado.get().getValor().getId());
        assertEquals("Valor Actualizado", resultado.get().getValor().getValor());
    }
    
}
