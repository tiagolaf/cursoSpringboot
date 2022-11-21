package com.tiago.helpdesk.api.security.jwt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.tiago.helpdesk.api.entity.User;
import com.tiago.helpdesk.api.enums.ProfileEnum;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class JwtUserFactory {

    private JwtUserFactory() {
    }
    
    public static JwtUser create(User user) {
        return new JwtUser(user.getId(), user.getEmail(), user.getPassword(), mapToGrantedAuthorities(user.getProfile()));
    }
    private static Collection<? extends GrantedAuthority> mapToGrantedAuthorities(ProfileEnum profileEnum) {
        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority(profileEnum.toString()));
        return authorities;
    }
}
