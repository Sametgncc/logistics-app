package com.logistics.backend.payload.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UpdateLoadRequest {

    @NotBlank(message = "Pickup city cannot be blank")
    private String pickupCity;

    @NotNull(message = "Pickup latitude cannot be null")
    private BigDecimal pickupLat;

    @NotNull(message = "Pickup longitude cannot be null")
    private BigDecimal pickupLng;

    @NotBlank(message = "Delivery city cannot be blank")
    private String deliveryCity;

    @NotNull(message = "Delivery latitude cannot be null")
    private BigDecimal deliveryLat;

    @NotNull(message = "Delivery longitude cannot be null")
    private BigDecimal deliveryLng;

    @NotNull(message = "Weight cannot be null")
    @DecimalMin(value = "0.01", message = "Weight must be greater than zero")
    private BigDecimal weightKg;

    @NotNull(message = "Volume cannot be null")
    @DecimalMin(value = "0.01", message = "Volume must be greater than zero")
    private BigDecimal volumeM3;

    @NotNull(message = "Pickup time cannot be null")
    private LocalDateTime pickupTime;

    private LocalDateTime deliveryDeadline;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
}