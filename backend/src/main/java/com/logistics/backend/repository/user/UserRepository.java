package com.logistics.backend.repository.user;

import com.logistics.backend.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsernameEquals(String username);

}
