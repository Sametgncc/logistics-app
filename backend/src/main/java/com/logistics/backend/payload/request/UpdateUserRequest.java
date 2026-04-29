package com.logistics.backend.payload.request;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String name;
    private String lastName;
    private String username;
    private String phone;
    private String address;
}
