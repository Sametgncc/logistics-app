package com.logistics.backend.service.user;

import com.logistics.backend.dto.RegisterRequest;
import com.logistics.backend.dto.VerifyRequest;
import com.logistics.backend.entity.user.User;
import com.logistics.backend.mapper.UserMapper;
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
    private final UserMapper userMapper;

    public void register(RegisterRequest request) {
        if(userRepository.existsByEmail(request.getEmail())){
            throw new RuntimeException("Email zaten kayıtlı");
        }
        String code=generateCode();

        User user = userMapper.toUser(request, passwordEncoder.encode(request.getPassword()), code, LocalDateTime.now().plusMinutes(5));

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
