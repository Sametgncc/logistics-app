package com.logistics.backend.payload.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class DriverLocationResponse {

    private Long routePlanId;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal accuracyMeters;
    private BigDecimal speedKph;
    private BigDecimal headingDegrees;
    private Instant capturedAt;
    private Instant receivedAt;
    private boolean stale;
}
