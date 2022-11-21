package com.tiago.helpdesk.api.security.jwt;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class JwtUser implements UserDetails {

    private final String id;
    private final String username;
    private final String password;
    private final Collection<? extends  GrantedAuthority> authority;
    
    public JwtUser(String id, String username, String password, Collection<? extends GrantedAuthority> authority) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.authority = authority;
    }

    @JsonIgnore
    public String getId() {
        return id;
    }

    public String getUsername() {
        return this.username;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authority;
    }


    @JsonIgnore
    @Override
    public String getPassword() {
        return this.password;
    }


    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }


    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }


    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }


    @JsonIgnore
    @Override
    public boolean isEnabled() {
        return true;
    }

}
