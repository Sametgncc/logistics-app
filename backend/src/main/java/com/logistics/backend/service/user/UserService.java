package com.logistics.backend.service.user;

import com.logistics.backend.entity.user.User;
import com.logistics.backend.mapper.UserMapper;
import com.logistics.backend.payload.messages.SuccessMessages;
import com.logistics.backend.payload.request.UpdateUserRequest;
import com.logistics.backend.payload.response.ResponseMessage;
import com.logistics.backend.payload.response.UserResponse;
import com.logistics.backend.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public ResponseMessage<UserResponse> getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        UserResponse userResponse = userMapper.toUserResponse(user);

        return ResponseMessage.<UserResponse>builder()
                .object(userResponse)
                .message(SuccessMessages.PROFILE_GET)
                .httpStatus(HttpStatus.OK)
                .build();
    }

    @Transactional
    public ResponseMessage<UserResponse> updateUserProfile(String email, UpdateUserRequest updateUserRequest) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        if (updateUserRequest.getName() != null && !updateUserRequest.getName().isEmpty()) {
            user.setName(updateUserRequest.getName());
        }
        if (updateUserRequest.getLastName() != null && !updateUserRequest.getLastName().isEmpty()) {
            user.setLastName(updateUserRequest.getLastName());
        }
        if (updateUserRequest.getUsername() != null && !updateUserRequest.getUsername().isEmpty()) {
            user.setUsername(updateUserRequest.getUsername());
        }
        if (updateUserRequest.getPhone() != null && !updateUserRequest.getPhone().isEmpty()) {
            user.setPhone(updateUserRequest.getPhone());
        }
        if (updateUserRequest.getAddress() != null && !updateUserRequest.getAddress().isEmpty()) {
            user.setAddress(updateUserRequest.getAddress());
        }

        User updatedUser = userRepository.save(user);
        UserResponse userResponse = userMapper.toUserResponse(updatedUser);

        return ResponseMessage.<UserResponse>builder()
                .object(userResponse)
                .message("Kullanıcı bilgileri başarıyla güncellendi")
                .httpStatus(HttpStatus.OK)
                .build();
    }
}
