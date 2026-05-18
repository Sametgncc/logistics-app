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
    private Boolean driverApproved;

    @JsonIgnore
    private String password;

    private Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(Long id, String email, String name, String password, Role role, Boolean driverApproved) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.password = password;
        this.driverApproved = driverApproved;
        List<GrantedAuthority> authorityList = new ArrayList<>();
        authorityList.add(new SimpleGrantedAuthority(role.name()));
        authorityList.add(new SimpleGrantedAuthority("ROLE_" + role.name()));

        // EĞER SÜRÜCÜ ONAYI VARSA, SİSTEMDE "DRIVER" GİBİ DAVRANMASINA İZİN VER
        if (Boolean.TRUE.equals(driverApproved)) {
            authorityList.add(new SimpleGrantedAuthority("DRIVER"));
            authorityList.add(new SimpleGrantedAuthority("ROLE_DRIVER"));
        }

        this.authorities = authorityList;
    }

    @Override
    public String getUsername() {
        return email;
    }
}