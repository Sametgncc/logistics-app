package com.logistics.backend.service.user;

import com.logistics.backend.entity.enums.Role;
import com.logistics.backend.entity.user.RoleType;
import com.logistics.backend.payload.request.CompleteProfileRequest;
import com.logistics.backend.payload.request.LoginRequest;
import com.logistics.backend.payload.request.RegisterRequest;
import com.logistics.backend.payload.request.SetPasswordRequest;
import com.logistics.backend.payload.request.VerifyRequest;
import com.logistics.backend.entity.user.User;
import com.logistics.backend.mapper.UserMapper;
import com.logistics.backend.payload.messages.SuccessMessages;
import com.logistics.backend.payload.response.ResponseMessage;
import com.logistics.backend.payload.response.UserResponse;
import com.logistics.backend.repository.user.RoleRepository;
import com.logistics.backend.repository.user.UserRepository;
import com.logistics.backend.security.jwt.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    private static final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[.@#$%^&+=!])(?=\\S+$).{8,}$";

    public ResponseMessage<UserResponse> register(RegisterRequest request) {
        String code = generateCode();
        Optional<User> existingUserOpt = userRepository.findByEmail(request.getEmail());
        User savedUser;

        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();
            if (existingUser.isEmailVerified()) {
                throw new RuntimeException("Bu email ile zaten kayıtlı ve doğrulanmış bir hesap var. Lütfen giriş yapın.");
            }
            existingUser.setVerificationCode(code);
            existingUser.setVerificationCodeExpire(LocalDateTime.now().plusMinutes(5));
            savedUser = userRepository.save(existingUser);
        } else {
            User newUser = userMapper.toUser(request, code, LocalDateTime.now().plusMinutes(5));
            /*RoleType driverRole = roleRepository.findByRole(Role.DRIVER)
                    .orElseThrow(() -> new RuntimeException("DRIVER rolü bulunamadı."));
            newUser.setRole(driverRole);*/
            savedUser = userRepository.save(newUser);
        }

        emailService.sendVerificationMail(request.getEmail(), code);

        return ResponseMessage.<UserResponse>builder()
                .message(SuccessMessages.MAIL_VERIFICATION_CODE_SENT)
                .httpStatus(HttpStatus.OK)
                .object(userMapper.toUserResponse(savedUser))
                .build();
    }

    private String generateCode() {
        return String.valueOf((int) ((Math.random() * 900000) + 100000));
    }

    public ResponseMessage<UserResponse> verify(VerifyRequest request) {
        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı."));

        if (user.isEmailVerified()) {
            throw new RuntimeException("E-posta zaten doğrulanmış.");
        }

        if (user.getVerificationCodeExpire() == null || user.getVerificationCodeExpire().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Kod süresi doldu. Lütfen tekrar kod isteyin.");
        }
        if (!user.getVerificationCode().equals(request.getCode())) {
            throw new RuntimeException("Kod yanlış");
        }

        user.setEmailVerified(true);
        User savedUser = userRepository.save(user);

        return ResponseMessage.<UserResponse>builder()
                .message(SuccessMessages.MAIL_VERIFIED)
                .httpStatus(HttpStatus.OK)
                .object(userMapper.toUserResponse(savedUser))
                .build();
    }

    public ResponseMessage<UserResponse> setPassword(SetPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı."));

        if (!user.isEmailVerified()) {
            throw new RuntimeException("Önce e-postanızı doğrulamanız gerekmektedir.");
        }

        if (user.getVerificationCode() != null && !user.getVerificationCode().equals(request.getCode())) {
            throw new RuntimeException("Geçersiz doğrulama kodu.");
        }

        if (request.getPassword() == null || !Pattern.matches(PASSWORD_PATTERN, request.getPassword())) {
            throw new RuntimeException("Şifre en az 8 karakter uzunluğunda olmalı, en az bir büyük harf, bir küçük harf, bir rakam ve bir özel karakter içermelidir.");
        }

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setVerificationCode(null);
        user.setVerificationCodeExpire(null);


        if (user.getRole() == null) {
             RoleType customerRole = roleRepository.findByRole(Role.CUSTOMER)
                .orElseThrow(() -> new RuntimeException("CUSTOMER rolü bulunamadı."));
             user.setRole(customerRole);
        }

        User savedUser = userRepository.save(user);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtUtils.generateJwtToken(authentication);

        UserResponse userResponse = userMapper.toUserResponse(savedUser);
        userResponse.setToken(token);

        return ResponseMessage.<UserResponse>builder()
                .message(SuccessMessages.PASSWORD_SET_SUCCESS)
                .httpStatus(HttpStatus.OK)
                .object(userResponse)
                .build();
    }

    public ResponseMessage<UserResponse> login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtUtils.generateJwtToken(authentication);

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı."));

        UserResponse userResponse = userMapper.toUserResponse(user);
        userResponse.setToken(token);

        return ResponseMessage.<UserResponse>builder()
                .message(SuccessMessages.LOGIN_SUCCESS)
                .httpStatus(HttpStatus.OK)
                .object(userResponse)
                .build();
    }

    public ResponseMessage<UserResponse> completeProfile(CompleteProfileRequest request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found."));

        user.setName(request.getName());
        user.setLastName(request.getLastName());
        if (request.getUsername() != null && !request.getUsername().isEmpty()) {
            user.setUsername(request.getUsername());
        }
        user.setAddress(request.getAddress());
        user.setPhone(request.getPhone());

        User savedUser = userRepository.save(user);

        return ResponseMessage.<UserResponse>builder()
                .message(SuccessMessages.USER_UPDATED)
                .httpStatus(HttpStatus.OK)
                .object(userMapper.toUserResponse(savedUser))
                .build();
    }
}
