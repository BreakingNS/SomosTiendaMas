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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
public class PrecioVarianteIntegrationTest {
    
    @Autowired
    private PrecioVarianteRepository repository;
    
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
        
        // When
        repository.deleteById(id);
        
        // Then
        Optional<PrecioVariante> resultado = repository.findById(id);
        assertFalse(resultado.isPresent());
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
    
}
