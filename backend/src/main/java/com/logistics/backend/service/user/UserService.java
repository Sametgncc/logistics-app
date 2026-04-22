package com.logistics.backend.service.user;

import com.logistics.backend.entity.user.User;
import com.logistics.backend.mapper.UserMapper;
import com.logistics.backend.payload.messages.SuccessMessages;
import com.logistics.backend.payload.response.ResponseMessage;
import com.logistics.backend.payload.response.UserResponse;
import com.logistics.backend.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

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
}
