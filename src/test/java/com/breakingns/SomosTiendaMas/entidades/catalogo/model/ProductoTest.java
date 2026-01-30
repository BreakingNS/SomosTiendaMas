package com.breakingns.SomosTiendaMas.entidades.catalogo.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProductoTest {

    @Test
    void addVariante_shouldAddAndSetProducto() {
        Producto p = new Producto();
        Variante v = new Variante();

        p.addVariante(v);

        assertNotNull(p.getVariantes());
        assertTrue(p.getVariantes().contains(v), "La variante debe agregarse a la lista de variantes del producto");
        assertEquals(p, v.getProducto(), "La variante debe referenciar al producto padre tras addVariante");
    }

    @Test
    void removeVariante_shouldRemoveAndClearParent() {
        Producto p = new Producto();
        Variante v = new Variante();

        p.addVariante(v);
        assertTrue(p.getVariantes().contains(v));

        p.removeVariante(v);

        assertFalse(p.getVariantes().contains(v), "La variante debe eliminarse de la lista tras removeVariante");
        assertNull(v.getProducto(), "La variante no debe referenciar más al producto tras removeVariante");
    }

    @Test
    void defaults_shouldBeInitialized() {
        Producto p = new Producto();

        // listas no nulas por defecto
        assertNotNull(p.getVariantes(), "La lista de variantes debe inicializarse (no null)");
        assertNotNull(p.getEtiquetas(), "La lista de etiquetas debe inicializarse (no null)");

        // valores por defecto de enums (según entidad)
        assertNotNull(p.getEstadoModeracion(), "Estado de moderación no debe ser null");
        assertNotNull(p.getEstadoProducto(), "Estado de producto no debe ser null");
        assertNotNull(p.getVisibilidad(), "Visibilidad no debe ser null");
        assertNotNull(p.getCondicion(), "Condición no debe ser null");
    }
}
