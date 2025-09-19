package com.breakingns.SomosTiendaMas.auth.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.breakingns.SomosTiendaMas.auth.model.Departamento;
import com.breakingns.SomosTiendaMas.auth.model.Localidad;
import com.breakingns.SomosTiendaMas.auth.model.Municipio;
import com.breakingns.SomosTiendaMas.auth.model.Provincia;
import com.breakingns.SomosTiendaMas.auth.repository.IDepartamentoRepository;
import com.breakingns.SomosTiendaMas.auth.repository.ILocalidadRepository;
import com.breakingns.SomosTiendaMas.auth.repository.IMunicipioRepository;
import com.breakingns.SomosTiendaMas.auth.repository.IProvinciaRepository;

import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class ImportLocalidadesService {

    @Autowired
    private ILocalidadRepository localidadRepository;
    @Autowired
    private IMunicipioRepository municipioRepository;
    @Autowired
    private IDepartamentoRepository departamentoRepository;
    @Autowired
    private IProvinciaRepository provinciaRepository;

    @Value("${localidades.excel.path}")
    private String excelPath;

    @Transactional
    public void importarLocalidades() throws Exception {
        FileInputStream fis = new FileInputStream(new File(excelPath));
        Workbook workbook = new XSSFWorkbook(fis);
        Sheet sheet = workbook.getSheetAt(0);
        Set<String> localidadesImportadas = new HashSet<>();

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            Cell nombreCell = row.getCell(0);
            Cell municipioCell = row.getCell(5);
            Cell departamentoCell = row.getCell(6);
            Cell provinciaCell = row.getCell(7);

            if (nombreCell == null || municipioCell == null || departamentoCell == null || provinciaCell == null) continue;

            String nombreLocalidad = nombreCell.getStringCellValue().trim();
            String municipioNombre = municipioCell.getStringCellValue().trim();
            String departamentoNombre = departamentoCell.getStringCellValue().trim();
            String provinciaNombre = provinciaCell.getStringCellValue().trim();

            Provincia provincia = provinciaRepository.findByNombre(provinciaNombre);
            Departamento departamento = departamentoRepository.findByNombreAndProvincia(departamentoNombre, provincia);
            Optional<Municipio> municipioOpt = municipioRepository.findByNombreAndDepartamento(municipioNombre, departamento);
            if (provincia == null || departamento == null || municipioOpt.isEmpty()) continue;
            Municipio municipio = municipioOpt.get();

            String claveUnica = nombreLocalidad + "|" + municipio.getId() + "|" + departamento.getId() + "|" + provincia.getId();
            if (!localidadesImportadas.contains(claveUnica)) {
                Optional<Localidad> existentes = localidadRepository.findByNombreAndMunicipioAndDepartamentoAndProvincia(
                    nombreLocalidad, municipio, departamento, provincia
                );
                /*
                List<Localidad> existentes = localidadRepository.findByNombreAndMunicipioAndDepartamentoAndProvincia(
                    nombreLocalidad, municipio, departamento, provincia
                );
                 */
                if (existentes == null) {
                    Localidad localidad = new Localidad();
                    localidad.setNombre(nombreLocalidad);
                    localidad.setMunicipio(municipio);
                    localidad.setDepartamento(departamento);
                    localidad.setProvincia(provincia);
                    localidadRepository.save(localidad);
                }
                localidadesImportadas.add(claveUnica);
            }
        }
        workbook.close();
        fis.close();
    }

    public long count() {
        return localidadRepository.count();
    }
}
