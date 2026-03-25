package com.logistics.backend.repository.user;

import com.logistics.backend.entity.enums.Role;
import com.logistics.backend.entity.user.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<RoleType, Long> {
    Optional<RoleType> findByRole(Role role);
}
