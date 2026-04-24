package com.logistics.backend.payload.request;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class CreateRoutePlanRequest {

    @NotNull
    private Long driverId;

    @NotNull
    private Long vehicleId;

    @NotBlank
    @Size(max = 100)
    private String originCity;

    @NotNull
    private BigDecimal originLat;

    @NotNull
    private BigDecimal originLng;

    @NotBlank
    @Size(max = 100)
    private String destinationCity;

    @NotNull
    private BigDecimal destinationLat;

    @NotNull
    private BigDecimal destinationLng;

    @NotNull
    private LocalDateTime departureTime;

    private LocalDateTime estimatedArrivalTime;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal availableCapacityKg;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal availableCapacityVolumeM3;

    @Size(max = 500)
    private String notes;

}