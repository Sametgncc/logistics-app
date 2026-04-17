package com.logistics.backend.payload.request;

import lombok.Data;

@Data
public class VerifyRequest {
    private String email;
    private String code;
}
