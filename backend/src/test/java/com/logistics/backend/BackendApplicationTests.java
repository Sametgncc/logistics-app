package com.logistics.backend;

import com.logistics.backend.security.service.UserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class BackendApplicationTests {

    private UserDetailsServiceImpl userDetailsService;

    @BeforeEach
    void setup() {
        userDetailsService = Mockito.mock(UserDetailsServiceImpl.class);
    }
    @Test
    void contextLoads() {
        assertNotNull(userDetailsService);
    }

    @TestConfiguration
    static class TestSecurityConfig {

        @Bean
        public DaoAuthenticationProvider authenticationProvider(UserDetailsServiceImpl userDetailsService) {
            DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
            provider.setUserDetailsService(userDetailsService); // mock inject edilecek
            provider.setPasswordEncoder(new BCryptPasswordEncoder());
            return provider;
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
            return authConfig.getAuthenticationManager();
        }
    }
}