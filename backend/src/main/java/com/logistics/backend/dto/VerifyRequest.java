package com.logistics.backend.dto;

import lombok.Data;

@Data
public class VerifyRequest {
    private String email;
    private String code;
}
