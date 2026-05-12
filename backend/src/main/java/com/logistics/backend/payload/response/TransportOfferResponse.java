package com.logistics.backend.payload.response;

import com.logistics.backend.entity.enums.OfferDirection;
import com.logistics.backend.entity.enums.OfferStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TransportOfferResponse {

    private Long id;

    private Long loadId;
    private String pickupCity;
    private String deliveryCity;

    private Long routePlanId;
    private String originCity;
    private String destinationCity;

    private Long driverId;
    private String driverName;
    private String driverEmail;

    private Long customerId;
    private String customerName;
    private String customerEmail;

    private Long offeredById;
    private String offeredByEmail;

    private OfferDirection direction;
    private BigDecimal price;
    private String message;

    private OfferStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;
}