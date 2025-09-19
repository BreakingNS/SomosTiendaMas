package com.breakingns.SomosTiendaMas.auth.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.breakingns.SomosTiendaMas.auth.model.Departamento;
import com.breakingns.SomosTiendaMas.auth.model.Municipio;

import java.util.Optional;

public interface IMunicipioRepository extends JpaRepository<Municipio, Long> {
    // Buscar municipio por nombre y departamento
    //Municipio findByNombreAndDepartamento(String nombre, Departamento departamento);
    Optional<Municipio> findByNombreAndDepartamento(String nombre, Departamento departamento);

    // Buscar municipio por nombre
    Municipio findByNombre(String nombre);

    // Buscar municipios por departamento
    List<Municipio> findByDepartamentoId(Long departamentoId);

    // Buscar municipios por localidad (consulta personalizada)
    @Query("SELECT m FROM Municipio m WHERE m.departamento.provincia.id = :provinciaId")
    List<Municipio> findByDepartamento_Provincia_Id(@Param("provinciaId") Long provinciaId);

    @Query("SELECT m FROM Municipio m JOIN m.localidades l WHERE l.id = :localidadId")
    List<Municipio> findByLocalidadId(@Param("localidadId") Long localidadId);
}
