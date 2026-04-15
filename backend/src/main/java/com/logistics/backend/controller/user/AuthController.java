package com.logistics.backend.controller.user;

import com.logistics.backend.dto.RegisterRequest;
import com.logistics.backend.dto.VerifyRequest;
import com.logistics.backend.service.user.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
         authService.register(request);
         return ResponseEntity.ok("Mail doğrulama kodu gönderildi");
     }

     @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody VerifyRequest request) {
         authService.verify(request);
         return ResponseEntity.ok("Mail doğrulandı.");
     }
}
