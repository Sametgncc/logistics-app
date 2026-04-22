package com.logistics.backend.controller.user;

import com.logistics.backend.payload.request.CompleteProfileRequest;
import com.logistics.backend.payload.request.LoginRequest;
import com.logistics.backend.payload.request.RegisterRequest;
import com.logistics.backend.payload.request.SetPasswordRequest;
import com.logistics.backend.payload.request.VerifyRequest;
import com.logistics.backend.payload.response.ResponseMessage;
import com.logistics.backend.payload.response.UserResponse;
import com.logistics.backend.service.user.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ResponseMessage<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/verify")
    public ResponseEntity<ResponseMessage<UserResponse>> verify(@Valid @RequestBody VerifyRequest request) {
        return ResponseEntity.ok(authService.verify(request));
    }

    @PostMapping("/set-password")
    public ResponseEntity<ResponseMessage<UserResponse>> setPassword(@Valid @RequestBody SetPasswordRequest request) {
        return ResponseEntity.ok(authService.setPassword(request));
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseMessage<UserResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/complete-profile")
    public ResponseEntity<ResponseMessage<UserResponse>> completeProfile(@Valid @RequestBody CompleteProfileRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(authService.completeProfile(request, userDetails.getUsername()));
    }
}
