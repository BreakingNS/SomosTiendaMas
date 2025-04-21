/*
package com.breakingns.SomosTiendaMas;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainTest {
    public static void main(String[] args) {
        String path = "keys/private.pem";
        InputStream inputStream = MainTest.class.getClassLoader().getResourceAsStream(path);

        if (inputStream == null) {
            System.out.println("❌ No se encontró el archivo en: " + path);
            return;
        }

        System.out.println("✅ Archivo encontrado. Contenido:");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            reader.lines().forEach(System.out::println);
        } catch (Exception e) {
            System.out.println("❌ Error al leer el archivo: " + e.getMessage());
        }
    }
}
*/