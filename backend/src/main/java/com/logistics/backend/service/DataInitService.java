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
            roleRepository.save(RoleType.valueOf(Role.ADMIN));
            roleRepository.save(RoleType.valueOf(Role.CUSTOMER));
            roleRepository.save(RoleType.valueOf(Role.DRIVER));
        }
    }
}
