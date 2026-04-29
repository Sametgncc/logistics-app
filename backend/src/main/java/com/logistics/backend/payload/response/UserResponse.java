package com.logistics.backend.payload.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.logistics.backend.entity.user.RoleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {
    private Long userId;
    private String email;
    private String name;
    private String lastName;
    private String username;
    private String address;
    private String phone;
    private RoleType role;
    private boolean emailVerified;
    private String token;
}
