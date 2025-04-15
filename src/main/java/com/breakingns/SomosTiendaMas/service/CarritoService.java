package com.breakingns.SomosTiendaMas.service;

import com.breakingns.SomosTiendaMas.model.Carrito;
import com.breakingns.SomosTiendaMas.domain.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.repository.ICarritoRepository;
import com.breakingns.SomosTiendaMas.domain.usuario.repository.IUsuarioRepository;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CarritoService implements ICarritoService{
    
    @Autowired
    IUsuarioRepository repoUsu;
    
    @Autowired
    ICarritoRepository repoCarrito;
    
    @Override
    public String crearCarrito(Long id_usuario) {

        Carrito carrito = new Carrito();

        Usuario usuario = repoUsu.findById(id_usuario)
                                 .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        carrito.setUsuario(usuario);

        repoCarrito.save(carrito);

        return "Carrito creado correctamente!";
    }
    
    @Override
    public Optional<Carrito> traerCarritoPorIdUsuario(Long id_usuario) {

        return  repoCarrito.findById(repoUsu.findById(id_usuario).orElse(null).getCarrito().getId_carrito());
    }
    
}
