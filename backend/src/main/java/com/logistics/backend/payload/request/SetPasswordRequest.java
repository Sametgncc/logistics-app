package com.logistics.backend.payload.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SetPasswordRequest {

    @NotBlank(message = "Email alanı boş olamaz")
    @Email(message = "Lütfen geçerli bir e-posta adresi giriniz")
    private String email;

    @NotBlank(message = "Şifre alanı boş olamaz")
    private String password;

    @NotBlank(message = "Doğrulama kodu boş olamaz")
    private String code;
}
