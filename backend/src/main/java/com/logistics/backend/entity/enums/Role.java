package com.logistics.backend.entity.enums;

import lombok.Getter;

@Getter
public enum Role {
    CUSTOMER("Customer"),
    DRIVER("Driver"),
    ADMIN("Admin");

    public final String name;

    Role(String name) {
        this.name = name;
    }

}
