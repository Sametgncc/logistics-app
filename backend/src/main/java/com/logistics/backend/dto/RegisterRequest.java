package com.logistics.backend.dto;

import com.logistics.backend.entity.enums.Role;
import lombok.Data;

@Data
public class RegisterRequest {
    private String name;
    private String username;
    private String password;
    private String email;
    private String phone;
    private Role role;

}
