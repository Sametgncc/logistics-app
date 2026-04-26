package com.logistics.backend.payload.response;

import com.logistics.backend.entity.enums.LoadStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class LoadResponse {

    private Long id;

    private Long customerId;
    private String customerName;
    private String customerEmail;

    private String pickupCity;
    private BigDecimal pickupLat;
    private BigDecimal pickupLng;

    private String deliveryCity;
    private BigDecimal deliveryLat;
    private BigDecimal deliveryLng;

    private BigDecimal weightKg;
    private BigDecimal volumeM3;

    private LocalDateTime pickupTime;
    private LocalDateTime deliveryDeadline;

    private String description;
    private LoadStatus status;
    private LocalDateTime createdAt;
}