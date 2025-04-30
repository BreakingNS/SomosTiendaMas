package com.breakingns.SomosTiendaMas.auth.model;

import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.breakingns.SomosTiendaMas.domain.usuario.model.Usuario;

public class UserAuthDetails implements UserDetails {

    private final Long id;
    private final String username;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final Usuario usuario; // 👈 nuevo campo

    public UserAuthDetails(Long id, String username, String password, Collection<? extends GrantedAuthority> authorities, Usuario usuario) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.authorities = authorities;
        this.usuario = usuario;
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public String getPassword() { return password; }
    @Override public String getUsername() { return username; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}