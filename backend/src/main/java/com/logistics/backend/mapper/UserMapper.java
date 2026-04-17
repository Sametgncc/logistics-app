package com.logistics.backend.mapper;

import com.logistics.backend.payload.request.RegisterRequest;
import com.logistics.backend.entity.user.User;
import com.logistics.backend.payload.response.UserResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class UserMapper {

    public User toUser(RegisterRequest request, String verificationCode, LocalDateTime verificationCodeExpire) {
        return User.builder()
                .email(request.getEmail())
                .verificationCode(verificationCode)
                .verificationCodeExpire(verificationCodeExpire)
                .emailVerified(false)
                .enabled(true)
                .build();
    }

    public UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .phone(user.getPhone())
                .role(user.getRole())
                .emailVerified(user.isEmailVerified())
                .build();
    }
}