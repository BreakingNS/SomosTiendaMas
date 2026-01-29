package com.breakingns.SomosTiendaMas.entidades.catalogo.service.impl;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.inventario.ConfirmacionVentaRequestDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.inventario.ReservaStockRequestDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.InventarioVariante;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.MovimientoInventario;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Producto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Variante;
import com.breakingns.SomosTiendaMas.entidades.catalogo.enums.TipoMovimientoInventario;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.InventarioVarianteRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.MovimientoInventarioRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.ProductoRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.VarianteRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IInventarioVarianteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ConfirmacionConcurrencyTest {

    @Autowired
    private ProductoRepository productoRepo;

    @Autowired
    private VarianteRepository varianteRepo;

    @Autowired
    private InventarioVarianteRepository invRepo;

    @Autowired
    private IInventarioVarianteService invService;

    @Autowired
    private MovimientoInventarioRepository movRepo;

    @Test
    public void concurrentConfirmShouldCreateSingleSalidaVenta() throws Exception {
        // Setup product/variante/inventory
        Producto p = new Producto();
        p.setNombre("TestProduct");
        p.setSlug("test-product-concurrency");
        productoRepo.save(p);

        Variante v = new Variante();
        v.setProducto(p);
        v.setSku("SKU-CONC-1");
        varianteRepo.save(v);

        InventarioVariante inv = InventarioVariante.builder()
                .variante(v)
                .onHand(10)
                .reserved(0)
                .build();
        inv = invRepo.save(inv);

        String orderRef = "ORD-CONC-1";
        int qty = 2;

        // create reservation
        ReservaStockRequestDTO reserva = new ReservaStockRequestDTO();
        reserva.setVarianteId(v.getId());
        reserva.setCantidad(qty);
        reserva.setOrderRef(orderRef);
        var resp = invService.reservarStock(reserva);
        assertTrue(resp.isOk(), "Reserva should succeed");

        // concurrent confirmations
        ConfirmacionVentaRequestDTO confirm = new ConfirmacionVentaRequestDTO();
        confirm.setOrderRef(orderRef);

        ExecutorService exec = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(2);

        Runnable task = () -> {
            try {
                start.await();
                invService.confirmarVenta(confirm);
            } catch (Exception e) {
                // capture but allow both to run
            } finally {
                done.countDown();
            }
        };

        exec.submit(task);
        exec.submit(task);

        // start both
        start.countDown();
        done.await();
        exec.shutdownNow();

        // verify only one SALIDA_VENTA
        List<MovimientoInventario> salidas = movRepo.findByOrderRefAndTipo(orderRef, TipoMovimientoInventario.SALIDA_VENTA);
        assertNotNull(salidas);
        assertEquals(1, salidas.size(), "Debe existir solo 1 movimiento SALIDA_VENTA para la misma orderRef");

        // verify inventory decreased once
        Optional<InventarioVariante> refreshed = invRepo.findByVarianteId(v.getId());
        assertTrue(refreshed.isPresent());
        InventarioVariante invAfter = refreshed.get();
        assertEquals(10 - qty, invAfter.getOnHand().intValue(), "onHand debe decrementar una sola vez");
        assertEquals(0, invAfter.getReserved().intValue(), "reserved debe llegar a 0 después de confirmación");
    }
}
