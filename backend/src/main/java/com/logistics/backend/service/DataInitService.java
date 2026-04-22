package com.logistics.backend.service;

import com.logistics.backend.entity.enums.Role;
import com.logistics.backend.entity.user.RoleType;
import com.logistics.backend.repository.user.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@RequiredArgsConstructor
public class DataInitService {

    private final RoleRepository roleRepository;

    @PostConstruct
    public void init() {
        if (roleRepository.count() == 0) {
            roleRepository.save(RoleType.builder().role(Role.ADMIN).build());
            roleRepository.save(RoleType.builder().role(Role.CUSTOMER).build());
            roleRepository.save(RoleType.builder().role(Role.DRIVER).build());
        }
    }
}
