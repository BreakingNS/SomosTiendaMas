package com.breakingns.SomosTiendaMas.entidades.catalogo.integration;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Vendedor;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.VendedorRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
public class VendedorIntegrationTest {
    
    @Autowired
    private VendedorRepository repository;
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    // === TESTS DE REPOSITORY ===
    
    @Test
    void save_DeberiaCrearVendedor_CuandoDatosValidos() {
        // Crear entidad con datos válidos
        Vendedor vendedor = new Vendedor();
        vendedor.setUserId(1L);
        vendedor.setNombre("Tienda Test");
        vendedor.setDescripcion("Descripción de prueba");
        vendedor.setRating(4.5);
        vendedor.setActivo(true);
        
        // Guardar en DB
        Vendedor guardado = repository.save(vendedor);
        
        // Verificar que ID fue generado
        assertNotNull(guardado.getId(), "El ID debe ser generado automáticamente");
        
        // Verificar que los datos se guardaron correctamente
        assertEquals(1L, guardado.getUserId());
        assertEquals("Tienda Test", guardado.getNombre());
        assertEquals("Descripción de prueba", guardado.getDescripcion());
        assertEquals(4.5, guardado.getRating());
        assertTrue(guardado.isActivo());
    }
    
    @Test
    void findById_DeberiaRetornarVendedor_CuandoExiste() {
        // Guardar entidad en DB
        Vendedor vendedor = new Vendedor();
        vendedor.setUserId(2L);
        vendedor.setNombre("Tienda Nueva");
        vendedor.setDescripcion("Nueva tienda");
        vendedor.setRating(5.0);
        vendedor.setActivo(true);
        Vendedor guardado = repository.save(vendedor);
        
        // Buscar por ID
        var resultado = repository.findById(guardado.getId());
        
        // Verificar que Optional.isPresent() es true
        assertTrue(resultado.isPresent(), "Debe encontrar el vendedor por ID");
        
        // Verificar datos coinciden
        Vendedor encontrado = resultado.get();
        assertEquals(2L, encontrado.getUserId());
        assertEquals("Tienda Nueva", encontrado.getNombre());
        assertEquals("Nueva tienda", encontrado.getDescripcion());
        assertEquals(5.0, encontrado.getRating());
        assertTrue(encontrado.isActivo());
    }
    
    @Test
    void findAll_DeberiaRetornarLista_CuandoExistenVendedores() {
        // Guardar 3 entidades diferentes
        Vendedor vendedor1 = new Vendedor();
        vendedor1.setUserId(10L);
        vendedor1.setNombre("Primera Tienda");
        vendedor1.setDescripcion("Descripción 1");
        vendedor1.setRating(4.0);
        vendedor1.setActivo(true);
        repository.save(vendedor1);
        
        Vendedor vendedor2 = new Vendedor();
        vendedor2.setUserId(20L);
        vendedor2.setNombre("Segunda Tienda");
        vendedor2.setDescripcion("Descripción 2");
        vendedor2.setRating(4.5);
        vendedor2.setActivo(true);
        repository.save(vendedor2);
        
        Vendedor vendedor3 = new Vendedor();
        vendedor3.setUserId(30L);
        vendedor3.setNombre("Tercera Tienda");
        vendedor3.setDescripcion("Descripción 3");
        vendedor3.setRating(3.5);
        vendedor3.setActivo(false);
        repository.save(vendedor3);
        
        // Llamar findAll()
        var lista = repository.findAll();
        
        // Verificar que retorna 3+ elementos (puede haber datos previos)
        assertTrue(lista.size() >= 3, "Debe retornar al menos 3 vendedores");
    }
    
    @Test
    void delete_DeberiaEliminarVendedor_CuandoExiste() {
        // Guardar entidad
        Vendedor vendedor = new Vendedor();
        vendedor.setUserId(99L);
        vendedor.setNombre("ParaBorrar");
        vendedor.setDescripcion("Para eliminar");
        vendedor.setRating(3.0);
        vendedor.setActivo(true);
        Vendedor guardado = repository.save(vendedor);
        Long id = guardado.getId();
        
        // Llamar deleteById
        repository.deleteById(id);
        
        // Verificar que repository.findById(id).isEmpty() es true
        assertTrue(repository.findById(id).isEmpty(), "El vendedor debe haber sido eliminado");
    }
    
    @Test
    void update_DeberiaActualizarVendedor_CuandoExiste() {
        // Guardar entidad con nombre "Original"
        Vendedor vendedor = new Vendedor();
        vendedor.setUserId(100L);
        vendedor.setNombre("Original");
        vendedor.setDescripcion("Descripción original");
        vendedor.setRating(2.5);
        vendedor.setActivo(false);
        Vendedor guardado = repository.save(vendedor);
        
        // Modificar nombre a "Actualizado"
        guardado.setNombre("Actualizado");
        guardado.setDescripcion("Descripción actualizada");
        guardado.setRating(5.0);
        guardado.setActivo(true);
        
        // Llamar repository.save(entidadModificada)
        repository.save(guardado);
        
        // Buscar por ID y verificar nombre es "Actualizado"
        var resultado = repository.findById(guardado.getId());
        assertTrue(resultado.isPresent());
        assertEquals("Actualizado", resultado.get().getNombre());
        assertEquals("Descripción actualizada", resultado.get().getDescripcion());
        assertEquals(5.0, resultado.get().getRating());
        assertTrue(resultado.get().isActivo());
    }
    
}
