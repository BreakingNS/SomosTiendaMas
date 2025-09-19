package com.breakingns.SomosTiendaMas.entidades.telefono.service;

import com.breakingns.SomosTiendaMas.entidades.telefono.model.CodigoArea;
import com.breakingns.SomosTiendaMas.entidades.telefono.repository.ICodigoAreaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import java.io.File;

@Service
public class ImportCodigosAreaService {
    @Autowired
    private ICodigoAreaRepository codigoAreaRepository;

    @Value("${codigos.area.excel.path}")
    private String excelPath;

    @Transactional
    public void importarCodigosArea() throws Exception {
        FileInputStream fis = new FileInputStream(new File(excelPath));
        Workbook workbook = new XSSFWorkbook(fis);
        Sheet sheet = workbook.getSheetAt(0);

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            // Leer código (puede ser numérico o string)
            String codigo = "";
            Cell cellCodigo = row.getCell(0);
            if (cellCodigo != null) {
                if (cellCodigo.getCellType() == CellType.NUMERIC) {
                    codigo = String.valueOf((long)cellCodigo.getNumericCellValue());
                } else {
                    codigo = cellCodigo.getStringCellValue().trim();
                }
            }

            // Leer localidad
            String localidad = "";
            Cell cellLocalidad = row.getCell(1);
            if (cellLocalidad != null) {
                if (cellLocalidad.getCellType() == CellType.NUMERIC) {
                    localidad = String.valueOf((long)cellLocalidad.getNumericCellValue());
                } else {
                    localidad = cellLocalidad.getStringCellValue().trim();
                }
            }

            // Leer provincia
            String provincia = "";
            Cell cellProvincia = row.getCell(2);
            if (cellProvincia != null) {
                if (cellProvincia.getCellType() == CellType.NUMERIC) {
                    provincia = String.valueOf((long)cellProvincia.getNumericCellValue());
                } else {
                    provincia = cellProvincia.getStringCellValue().trim();
                }
            }

            if (codigoAreaRepository.findByCodigo(codigo).isEmpty()) {
                CodigoArea ca = new CodigoArea();
                ca.setCodigo(codigo);
                ca.setLocalidad(localidad);
                ca.setProvincia(provincia);
                codigoAreaRepository.save(ca);
            }
        }
        workbook.close();
        fis.close();
    }

    public long count() {
        return codigoAreaRepository.count();
    }
}
