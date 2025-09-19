package com.breakingns.SomosTiendaMas.auth.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.breakingns.SomosTiendaMas.auth.model.Departamento;
import com.breakingns.SomosTiendaMas.auth.model.Municipio;
import com.breakingns.SomosTiendaMas.auth.model.Provincia;
import com.breakingns.SomosTiendaMas.auth.repository.IDepartamentoRepository;
import com.breakingns.SomosTiendaMas.auth.repository.IMunicipioRepository;
import com.breakingns.SomosTiendaMas.auth.repository.IProvinciaRepository;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class ImportMunicipiosService {

    @Autowired
    private IMunicipioRepository municipioRepository;
    @Autowired
    private IProvinciaRepository provinciaRepository;
    @Autowired
    private IDepartamentoRepository departamentoRepository;

    @Value("${localidades.excel.path}")
    private String excelPath;

    @Transactional
    public void importarMunicipios() throws Exception {
        FileInputStream fis = new FileInputStream(new File(excelPath));
        Workbook workbook = new XSSFWorkbook(fis);
        Sheet sheet = workbook.getSheetAt(0);

        Set<String> municipiosImportados = new HashSet<>();

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            String municipioNombre = row.getCell(5).getStringCellValue().trim();
            String departamentoNombre = row.getCell(6).getStringCellValue().trim();
            String provinciaNombre = row.getCell(7).getStringCellValue().trim();

            Provincia provincia = provinciaRepository.findByNombre(provinciaNombre);
            Departamento departamento = departamentoRepository.findByNombreAndProvincia(departamentoNombre, provincia);

            if (departamento == null) continue;

            String claveUnica = municipioNombre + "|" + departamento.getId();
            
            if (!municipiosImportados.contains(claveUnica)) {
                Optional<Municipio> municipioOpt = municipioRepository.findByNombreAndDepartamento(municipioNombre, departamento);
                if (municipioOpt.isEmpty()) {
                    Municipio municipio = new Municipio();
                    municipio.setNombre(municipioNombre);
                    municipio.setDepartamento(departamento);
                    municipioRepository.save(municipio);
                }
                municipiosImportados.add(claveUnica);
            }
        }
        workbook.close();
        fis.close();
    }

    public long count() {
        return municipioRepository.count();
    }
}