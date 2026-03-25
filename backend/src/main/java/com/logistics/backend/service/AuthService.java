package com.logistics.backend.service;

import com.logistics.backend.dto.RegisterRequest;
import com.logistics.backend.dto.VerifyRequest;
import com.logistics.backend.entity.user.RoleType;
import com.logistics.backend.entity.user.User;
import com.logistics.backend.repository.user.RoleRepository;
import com.logistics.backend.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final RoleRepository roleRepository;

    public void register(RegisterRequest request) {
        if(userRepository.existsByEmail(request.getEmail())){
            throw new RuntimeException("Email zaten kayıtlı");
        }
        String code=generateCode();

        RoleType role = roleRepository
                .findByRole(request.getRole())
                .orElseThrow(() -> new RuntimeException("Role bulunamadı"));

        User user = new User();
        user.setName(request.getName());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole(role);

        user.setVerificationCode(code);
        user.setEmailVerified(false);
        user.setVerificationCodeExpire(LocalDateTime.now().plusMinutes(5));

        userRepository.save(user);

        emailService.sendVerificationMail(user.getEmail(),  code);

    }

    private String generateCode() {
        return String.valueOf((int)((Math.random()*900000)+100000));
    }

    public void verify(VerifyRequest request) {
        User user=userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(()-> new RuntimeException("Kullanıcı bulunamadı."));
        if(user.getVerificationCodeExpire().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Kod süresi doldu.");
        }
        if (!user.getVerificationCode().equals(request.getCode())) {
            throw new RuntimeException("Kod yanlış");
        }

        user.setEmailVerified(true);
        user.setVerificationCode(null);

        userRepository.save(user);
    }
}
