package com.tiago.helpdesk.api.security.controller;

import javax.servlet.http.HttpServletRequest;

import com.tiago.helpdesk.api.entity.User;
import com.tiago.helpdesk.api.security.jwt.JwtAuthenticationRequest;
import com.tiago.helpdesk.api.security.jwt.JwtTokenUtil;
import com.tiago.helpdesk.api.security.model.CurrentUser;
import com.tiago.helpdesk.api.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.Data;


@RestController
@CrossOrigin(origins = "*")
@Data
public class AuthenticationRestController {

    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private UserService userService;

    @PostMapping(value="/api/auth")
    public ResponseEntity<?>  createAuthenticationToken(@RequestBody JwtAuthenticationRequest authenticationRequest) {
       final Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authenticationRequest.getEmail(), authenticationRequest.getPassword())); 
       SecurityContextHolder.getContext().setAuthentication(authentication); 
       final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getEmail());
       final String token = jwtTokenUtil.generateTokenString(userDetails);
       final User user = userService.findByEmail(authenticationRequest.getEmail());
       user.setPassword(null);

       return ResponseEntity.ok(new CurrentUser(token, user));
    }

    @PostMapping(value="/api/refresh")
    public ResponseEntity<?> refreshAndGetAuthenticationToken(HttpServletRequest request) {
        String token = request.getHeader("Authentication");
        String username = jwtTokenUtil.getUsernameFromToken(token);
        final User user = userService.findByEmail(username);

        if(jwtTokenUtil.canTokenBeRefreshed(token)){
            String refreshToken = jwtTokenUtil.refreshToken(token);
            return ResponseEntity.ok(new CurrentUser(refreshToken, user));
        } else {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    
}