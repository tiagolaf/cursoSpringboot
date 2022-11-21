package com.tiago.helpdesk.api.security.service;

import com.tiago.helpdesk.api.entity.User;
import com.tiago.helpdesk.api.security.jwt.JwtUserFactory;
import com.tiago.helpdesk.api.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class JwtUserDetailServiceImpl implements UserDetailsService{

    @Autowired
    UserService userService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userService.findByEmail(email);

        if(user == null) {
            throw new UsernameNotFoundException(String.format( "Nenhum usuário encontrado com o email: %s", email));
        } else {
            return JwtUserFactory.create(user);
        }
    }
    
}
