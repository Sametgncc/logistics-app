package com.logistics.backend.mapper;

import com.logistics.backend.dto.RegisterRequest;
import com.logistics.backend.entity.user.RoleType;
import com.logistics.backend.entity.user.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class UserMapper {

    public User toUser(RegisterRequest request, String encodedPassword, String verificationCode, LocalDateTime verificationCodeExpire) {
        User user = new User();
        user.setName(request.getName());
        user.setUsername(request.getUsername());
        user.setPassword(encodedPassword);
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole(RoleType.valueOf(request.getRole()));
        user.setVerificationCode(verificationCode);
        user.setEmailVerified(false);
        user.setVerificationCodeExpire(verificationCodeExpire);
        return user;
    }
}
