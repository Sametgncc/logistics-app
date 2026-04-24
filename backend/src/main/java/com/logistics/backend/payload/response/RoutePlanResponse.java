package com.logistics.backend.payload.response;

import com.logistics.backend.entity.enums.RoutePlanStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class RoutePlanResponse {

    private Long id;
    private Long driverId;
    private String driverFullName;
    private Long vehicleId;
    private String plateNumber;

    private String originCity;
    private BigDecimal originLat;
    private BigDecimal originLng;

    private String destinationCity;
    private BigDecimal destinationLat;
    private BigDecimal destinationLng;

    private LocalDateTime departureTime;
    private LocalDateTime estimatedArrivalTime;

    private BigDecimal availableCapacityKg;
    private BigDecimal availableCapacityVolumeM3;

    private RoutePlanStatus status;
    private String notes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}