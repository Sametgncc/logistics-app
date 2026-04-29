package com.logistics.backend.payload.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CompleteProfileRequest {
    @NotBlank(message = "İsim alanı boş olamaz")
    private String name;
    @NotBlank(message = "Soyisim alanı boş olamaz")
    private String lastName;
    private String username;
    @NotBlank(message = "Adres alanı boş olamaz")
    private String address;
    @NotBlank(message = "Telefon alanı boş olamaz")
    private String phone;
}
