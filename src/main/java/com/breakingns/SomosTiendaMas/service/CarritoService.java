package com.breakingns.SomosTiendaMas.service;

import com.breakingns.SomosTiendaMas.model.Carrito;
import com.breakingns.SomosTiendaMas.domain.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.repository.ICarritoRepository;
import com.breakingns.SomosTiendaMas.domain.usuario.repository.IUsuarioRepository;
import com.breakingns.SomosTiendaMas.security.exception.UsuarioNoEncontradoException;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class CarritoService implements ICarritoService{

    private final IUsuarioRepository repoUsu;
    private final ICarritoRepository repoCarrito;

    public CarritoService(IUsuarioRepository repoUsu, ICarritoRepository repoCarrito) {
        this.repoUsu = repoUsu;
        this.repoCarrito = repoCarrito;
    }
    
    @Override
    public String crearCarrito(Long id_usuario) {
        Usuario usuario = repoUsu.findById(id_usuario)
                                 .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado"));

        Carrito carrito = new Carrito();
        carrito.setUsuario(usuario);
        repoCarrito.save(carrito);

        return "Carrito creado correctamente!";
    }
    
    @Override
    public Optional<Carrito> traerCarritoPorIdUsuario(Long id_usuario) {
        return repoCarrito.findByUsuario_IdUsuario(id_usuario);
    }
    
}
