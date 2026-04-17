package com.logistics.backend.payload.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Lütfen geçerli bir e-posta adresi giriniz")
    @Email(message = "Geçerli bir e-posta adresi değil")
    private String email;
}
