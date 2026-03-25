package com.logistics.backend.repository.user;

import com.logistics.backend.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsernameEquals(String username);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

}
