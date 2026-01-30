package com.breakingns.SomosTiendaMas.entidades.catalogo.integration;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.*;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.*;
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
public class VarianteOpcionValorIntegrationTest {
    
    @Autowired
    private VarianteOpcionValorRepository repository;
    
    @Autowired
    private VarianteRepository varianteRepository;
    
    @Autowired
    private OpcionRepository opcionRepository;
    
    @Autowired
    private OpcionValorRepository opcionValorRepository;
    
    @Autowired
    private ProductoRepository productoRepository;
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @BeforeEach
    void sincronizarSecuencia() {
        entityManager.createNativeQuery("SELECT setval('variante_opcion_valor_id_seq', (SELECT COALESCE(MAX(id), 1) FROM variante_opcion_valor))")
                     .getSingleResult();
    }
    
    // === TESTS DE REPOSITORY ===
    
    @Test
    void save_DeberiaCrearVarianteOpcionValor_CuandoDatosValidos() {
        // Crear producto y variante padre
        Producto producto = new Producto();
        producto.setNombre("Producto Test");
        producto.setSlug("producto-" + System.currentTimeMillis());
        producto = productoRepository.save(producto);
        
        Variante variante = new Variante();
        variante.setProducto(producto);
        variante.setSku("SKU-" + System.currentTimeMillis());
        variante.setEsDefault(true);
        variante.setActivo(true);
        variante = varianteRepository.save(variante);
        
        // Crear opción y opción valor padre
        Opcion opcion = new Opcion();
        opcion.setNombre("Color");
        opcion.setOrden(1);
        opcion.setTipo("select");
        opcion = opcionRepository.save(opcion);
        
        OpcionValor opcionValor = new OpcionValor();
        opcionValor.setOpcion(opcion);
        opcionValor.setValor("Rojo");
        opcionValor.setSlug("rojo-test");
        opcionValor.setOrden(1);
        opcionValor = opcionValorRepository.save(opcionValor);
        
        // Crear entidad con datos válidos
        VarianteOpcionValor vov = new VarianteOpcionValor();
        vov.setVariante(variante);
        vov.setOpcion(opcion);
        vov.setOpcionValor(opcionValor);
        
        // Guardar en DB
        VarianteOpcionValor guardada = repository.save(vov);
        
        // Verificar que ID fue generado
        assertNotNull(guardada.getId(), "El ID debe ser generado automáticamente");
        
        // Verificar que los datos se guardaron correctamente
        assertNotNull(guardada.getVariante());
        assertNotNull(guardada.getOpcion());
        assertNotNull(guardada.getOpcionValor());
        assertEquals("Rojo", guardada.getOpcionValor().getValor());
    }
    
    @Test
    void findById_DeberiaRetornarVarianteOpcionValor_CuandoExiste() {
        // Crear producto y variante padre
        Producto producto = new Producto();
        producto.setNombre("Producto Test 2");
        producto.setSlug("producto-2-" + System.currentTimeMillis());
        producto = productoRepository.save(producto);
        
        Variante variante = new Variante();
        variante.setProducto(producto);
        variante.setSku("SKU-2-" + System.currentTimeMillis());
        variante.setEsDefault(true);
        variante.setActivo(true);
        variante = varianteRepository.save(variante);
        
        // Crear opción y opción valor padre
        Opcion opcion = new Opcion();
        opcion.setNombre("Talla");
        opcion.setOrden(1);
        opcion.setTipo("select");
        opcion = opcionRepository.save(opcion);
        
        OpcionValor opcionValor = new OpcionValor();
        opcionValor.setOpcion(opcion);
        opcionValor.setValor("M");
        opcionValor.setSlug("m-test");
        opcionValor.setOrden(1);
        opcionValor = opcionValorRepository.save(opcionValor);
        
        // Guardar entidad en DB
        VarianteOpcionValor vov = new VarianteOpcionValor();
        vov.setVariante(variante);
        vov.setOpcion(opcion);
        vov.setOpcionValor(opcionValor);
        VarianteOpcionValor guardada = repository.save(vov);
        
        // Buscar por ID
        var resultado = repository.findById(guardada.getId());
        
        // Verificar que Optional.isPresent() es true
        assertTrue(resultado.isPresent(), "Debe encontrar la variante opción valor por ID");
        
        // Verificar datos coinciden
        VarianteOpcionValor encontrada = resultado.get();
        assertEquals("M", encontrada.getOpcionValor().getValor());
    }
    
    @Test
    void findAll_DeberiaRetornarLista_CuandoExistenVarianteOpcionValores() {
        // Crear producto y variante padre
        Producto producto = new Producto();
        producto.setNombre("Producto Test 3");
        producto.setSlug("producto-3-" + System.currentTimeMillis());
        producto = productoRepository.save(producto);
        
        Variante variante = new Variante();
        variante.setProducto(producto);
        variante.setSku("SKU-3-" + System.currentTimeMillis());
        variante.setEsDefault(true);
        variante.setActivo(true);
        variante = varianteRepository.save(variante);
        
        // Crear opción padre
        Opcion opcion = new Opcion();
        opcion.setNombre("Material");
        opcion.setOrden(1);
        opcion.setTipo("select");
        opcion = opcionRepository.save(opcion);
        
        // Guardar 3 entidades diferentes con diferentes valores
        OpcionValor ov1 = new OpcionValor();
        ov1.setOpcion(opcion);
        ov1.setValor("Algodón");
        ov1.setSlug("algodon-test");
        ov1.setOrden(1);
        ov1 = opcionValorRepository.save(ov1);
        
        VarianteOpcionValor vov1 = new VarianteOpcionValor();
        vov1.setVariante(variante);
        vov1.setOpcion(opcion);
        vov1.setOpcionValor(ov1);
        repository.save(vov1);
        
        // Crear segunda variante para segunda asociación
        Variante variante2 = new Variante();
        variante2.setProducto(producto);
        variante2.setSku("SKU-3B-" + System.currentTimeMillis());
        variante2.setEsDefault(false);
        variante2.setActivo(true);
        variante2 = varianteRepository.save(variante2);
        
        OpcionValor ov2 = new OpcionValor();
        ov2.setOpcion(opcion);
        ov2.setValor("Poliéster");
        ov2.setSlug("poliester-test");
        ov2.setOrden(2);
        ov2 = opcionValorRepository.save(ov2);
        
        VarianteOpcionValor vov2 = new VarianteOpcionValor();
        vov2.setVariante(variante2);
        vov2.setOpcion(opcion);
        vov2.setOpcionValor(ov2);
        repository.save(vov2);
        
        // Crear tercera variante para tercera asociación
        Variante variante3 = new Variante();
        variante3.setProducto(producto);
        variante3.setSku("SKU-3C-" + System.currentTimeMillis());
        variante3.setEsDefault(false);
        variante3.setActivo(true);
        variante3 = varianteRepository.save(variante3);
        
        OpcionValor ov3 = new OpcionValor();
        ov3.setOpcion(opcion);
        ov3.setValor("Lana");
        ov3.setSlug("lana-test");
        ov3.setOrden(3);
        ov3 = opcionValorRepository.save(ov3);
        
        VarianteOpcionValor vov3 = new VarianteOpcionValor();
        vov3.setVariante(variante3);
        vov3.setOpcion(opcion);
        vov3.setOpcionValor(ov3);
        repository.save(vov3);
        
        // Llamar findAll()
        var lista = repository.findAll();
        
        // Verificar que retorna 3+ elementos (puede haber datos previos)
        assertTrue(lista.size() >= 3, "Debe retornar al menos 3 variante opción valores");
    }
    
    @Test
    void delete_DeberiaEliminarVarianteOpcionValor_CuandoExiste() {
        // Crear producto y variante padre
        Producto producto = new Producto();
        producto.setNombre("Producto Test 4");
        producto.setSlug("producto-4-" + System.currentTimeMillis());
        producto = productoRepository.save(producto);
        
        Variante variante = new Variante();
        variante.setProducto(producto);
        variante.setSku("SKU-4-" + System.currentTimeMillis());
        variante.setEsDefault(true);
        variante.setActivo(true);
        variante = varianteRepository.save(variante);
        
        // Crear opción y opción valor padre
        Opcion opcion = new Opcion();
        opcion.setNombre("Acabado");
        opcion.setOrden(1);
        opcion.setTipo("select");
        opcion = opcionRepository.save(opcion);
        
        OpcionValor opcionValor = new OpcionValor();
        opcionValor.setOpcion(opcion);
        opcionValor.setValor("ParaBorrar");
        opcionValor.setSlug("para-borrar-test");
        opcionValor.setOrden(1);
        opcionValor = opcionValorRepository.save(opcionValor);
        
        // Guardar entidad
        VarianteOpcionValor vov = new VarianteOpcionValor();
        vov.setVariante(variante);
        vov.setOpcion(opcion);
        vov.setOpcionValor(opcionValor);
        VarianteOpcionValor guardada = repository.save(vov);
        Long id = guardada.getId();
        
        // Llamar deleteById
        repository.deleteById(id);
        
        // Verificar que repository.findById(id).isEmpty() es true
        assertTrue(repository.findById(id).isEmpty(), "La variante opción valor debe haber sido eliminada");
    }
    
    @Test
    void update_DeberiaActualizarVarianteOpcionValor_CuandoExiste() {
        // Crear producto y variante padre
        Producto producto = new Producto();
        producto.setNombre("Producto Test 5");
        producto.setSlug("producto-5-" + System.currentTimeMillis());
        producto = productoRepository.save(producto);
        
        Variante variante = new Variante();
        variante.setProducto(producto);
        variante.setSku("SKU-5-" + System.currentTimeMillis());
        variante.setEsDefault(true);
        variante.setActivo(true);
        variante = varianteRepository.save(variante);
        
        // Crear opción padre
        Opcion opcion = new Opcion();
        opcion.setNombre("Estilo");
        opcion.setOrden(1);
        opcion.setTipo("select");
        opcion = opcionRepository.save(opcion);
        
        // Crear opción valor original
        OpcionValor ovOriginal = new OpcionValor();
        ovOriginal.setOpcion(opcion);
        ovOriginal.setValor("Original");
        ovOriginal.setSlug("original-test");
        ovOriginal.setOrden(1);
        ovOriginal = opcionValorRepository.save(ovOriginal);
        
        // Guardar entidad con valor original
        VarianteOpcionValor vov = new VarianteOpcionValor();
        vov.setVariante(variante);
        vov.setOpcion(opcion);
        vov.setOpcionValor(ovOriginal);
        VarianteOpcionValor guardada = repository.save(vov);
        
        // Crear nuevo opción valor "Actualizado"
        OpcionValor ovActualizado = new OpcionValor();
        ovActualizado.setOpcion(opcion);
        ovActualizado.setValor("Actualizado");
        ovActualizado.setSlug("actualizado-test");
        ovActualizado.setOrden(2);
        ovActualizado = opcionValorRepository.save(ovActualizado);
        
        // Modificar valor a "Actualizado"
        guardada.setOpcionValor(ovActualizado);
        
        // Llamar repository.save(entidadModificada)
        repository.save(guardada);
        
        // Buscar por ID y verificar valor es "Actualizado"
        var resultado = repository.findById(guardada.getId());
        assertTrue(resultado.isPresent());
        assertEquals("Actualizado", resultado.get().getOpcionValor().getValor());
        assertEquals(ovActualizado.getId(), resultado.get().getOpcionValor().getId());
    }
    
}
