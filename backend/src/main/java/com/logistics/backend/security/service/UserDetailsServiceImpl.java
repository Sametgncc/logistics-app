package com.logistics.backend.security.service;

import com.logistics.backend.entity.user.User;
import com.logistics.backend.repository.user.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email).orElse(null);

        if(user != null){
            return new UserDetailsImpl(
                    user.getId(),
                    user.getEmail(),
                    user.getName(),
                    user.getPassword()
            );
        }
        throw new UsernameNotFoundException("User' " + email + " not found");
    }
}
