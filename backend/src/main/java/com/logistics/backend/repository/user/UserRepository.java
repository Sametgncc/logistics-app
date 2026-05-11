package com.logistics.backend.repository.user;

import com.logistics.backend.entity.user.User;
import com.logistics.backend.entity.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsernameEquals(String username);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByRole_Role(Role role);
    long countByRole_Role(Role role);
    long countByDriverApproved(Boolean driverApproved);
    long countByEnabled(Boolean enabled);

}
