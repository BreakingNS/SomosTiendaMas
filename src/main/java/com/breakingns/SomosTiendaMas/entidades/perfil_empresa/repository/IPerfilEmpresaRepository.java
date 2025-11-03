package com.breakingns.SomosTiendaMas.entidades.perfil_empresa.repository;

import com.breakingns.SomosTiendaMas.entidades.perfil_empresa.model.PerfilEmpresa;
import com.breakingns.SomosTiendaMas.entidades.usuario.model.Usuario;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IPerfilEmpresaRepository extends JpaRepository<PerfilEmpresa, Long> {
    Optional<PerfilEmpresa> findByCuit(String cuit);
    boolean existsByCuit(String cuit);
    Optional<PerfilEmpresa> findByRazonSocial(String string);
    Optional<PerfilEmpresa> findByUsuario(Usuario usuario);
    List<PerfilEmpresa> findByEstadoAprobado(PerfilEmpresa.EstadoAprobado estadoAprobado);

}
