package com.logistics.backend.security.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.logistics.backend.entity.enums.Role;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailsImpl implements UserDetails {

    private Long id;
    private String email;
    private String name;

    @JsonIgnore
    private String password;

    private Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(Long id, String email, String name, String password, Role role) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.password = password;

        this.authorities = List.of(
                new SimpleGrantedAuthority(role.name()),           // DRIVER
                new SimpleGrantedAuthority("ROLE_" + role.name())  // ROLE_DRIVER
        );
    }

    @Override
    public String getUsername() {
        return email;
    }
}