package com.breakingns.SomosTiendaMas.auth.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.breakingns.SomosTiendaMas.auth.model.Departamento;
import com.breakingns.SomosTiendaMas.auth.model.Provincia;
import com.breakingns.SomosTiendaMas.auth.repository.IDepartamentoRepository;
import com.breakingns.SomosTiendaMas.auth.repository.IProvinciaRepository;

import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashSet;
import java.util.Set;

@Service
public class ImportDepartamentosService {

    @Autowired
    private IDepartamentoRepository departamentoRepository;
    @Autowired
    private IProvinciaRepository provinciaRepository;

    @Value("${localidades.excel.path}")
    private String excelPath;

    @Transactional
    public void importarDepartamentos() throws Exception {
        FileInputStream fis = new FileInputStream(new File(excelPath));
        Workbook workbook = new XSSFWorkbook(fis);
        Sheet sheet = workbook.getSheetAt(0);

        Set<String> departamentosImportados = new HashSet<>();

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            String provinciaNombre = row.getCell(7).getStringCellValue().trim();
            String departamentoNombre = row.getCell(6).getStringCellValue().trim();

            Provincia provincia = provinciaRepository.findByNombre(provinciaNombre);
            if (provincia == null) continue;

            // Usá nombre + provincia como clave única
            String claveUnica = departamentoNombre + "|" + provincia.getId();

            if (!departamentosImportados.contains(claveUnica)) {
                if (departamentoRepository.findByNombreAndProvincia(departamentoNombre, provincia) == null) {
                    Departamento departamento = new Departamento();
                    departamento.setNombre(departamentoNombre);
                    departamento.setProvincia(provincia);
                    departamentoRepository.save(departamento);
                }
                departamentosImportados.add(claveUnica);
            }
        }
        workbook.close();
        fis.close();
    }

    public long count() {
        return departamentoRepository.count();
    }
}
