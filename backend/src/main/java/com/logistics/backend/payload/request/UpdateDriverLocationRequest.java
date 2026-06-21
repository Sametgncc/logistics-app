package com.logistics.backend.payload.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDriverLocationRequest {

    @NotNull
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    private BigDecimal latitude;

    @NotNull
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    private BigDecimal longitude;

    @DecimalMin(value = "0.0")
    private BigDecimal accuracyMeters;

    @DecimalMin(value = "0.0")
    private BigDecimal speedKph;

    @DecimalMin(value = "0.0")
    @DecimalMax(value = "360.0")
    private BigDecimal headingDegrees;

    @NotNull
    private Instant capturedAt;
}
