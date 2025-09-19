package com.breakingns.SomosTiendaMas.auth.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.breakingns.SomosTiendaMas.auth.model.Departamento;
import com.breakingns.SomosTiendaMas.auth.model.Localidad;
import com.breakingns.SomosTiendaMas.auth.model.Municipio;
import com.breakingns.SomosTiendaMas.auth.model.Provincia;

public interface ILocalidadRepository extends JpaRepository<Localidad, Long> {
    Optional<Localidad> findByNombreAndMunicipioAndDepartamentoAndProvincia(
        String nombre,
        Municipio municipio,
        Departamento departamento,
        Provincia provincia
    );
    
    /*List<Localidad> findByNombreAndMunicipioAndDepartamentoAndProvincia(
        String nombre,
        Municipio municipio,
        Departamento departamento,
        Provincia provincia
    );*/

    //Localidad findByNombre(String nombre);
    List<Localidad> findByDepartamentoId(Long departamentoId);
    List<Localidad> findByProvinciaId(Long provinciaId);

    @Query("SELECT l FROM Localidad l WHERE l.municipio.id = :municipioId")
    List<Localidad> findByMunicipioId(@Param("municipioId") Long municipioId);
}