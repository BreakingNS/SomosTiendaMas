package com.breakingns.SomosTiendaMas.auth.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.breakingns.SomosTiendaMas.auth.model.Pais;
import com.breakingns.SomosTiendaMas.auth.model.Provincia;
import com.breakingns.SomosTiendaMas.auth.repository.IPaisRepository;
import com.breakingns.SomosTiendaMas.auth.repository.IProvinciaRepository;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashSet;
import java.util.Set;

@Service
public class ImportProvinciasService {

    @Autowired
    private IProvinciaRepository provinciaRepository;
    @Autowired
    private IPaisRepository paisRepository;

    @Value("${localidades.excel.path}")
    private String excelPath;

    @Transactional
    public void importarProvincias() throws Exception {
        FileInputStream fis = new FileInputStream(new File(excelPath));
        Workbook workbook = new XSSFWorkbook(fis);
        Sheet sheet = workbook.getSheetAt(0);

        // usar búsqueda case-insensitive; crear país en mayúsculas si no existe
        Pais pais = paisRepository.findByNombreIgnoreCase("Argentina");
        if (pais == null) pais = paisRepository.save(new Pais("ARGENTINA"));

        Set<String> provinciasImportadas = new HashSet<>();

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            String provinciaNombre = row.getCell(7).getStringCellValue().trim();
            if (!provinciasImportadas.contains(provinciaNombre)) {
                if (provinciaRepository.findByNombreAndPais(provinciaNombre, pais) == null) {
                    Provincia provincia = new Provincia(provinciaNombre, pais);
                    provinciaRepository.save(provincia);
                }
                provinciasImportadas.add(provinciaNombre);
            }
        }
        workbook.close();
        fis.close();
    }

    public long count() {
        return provinciaRepository.count();
    }
}