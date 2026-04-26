package com.logistics.backend.payload.response;

import com.logistics.backend.entity.enums.MatchStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class LoadMatchResponse {

    private Long id;

    private Long loadId;
    private String pickupCity;
    private String deliveryCity;
    private BigDecimal weightKg;
    private BigDecimal volumeM3;

    private Long routePlanId;
    private Long driverId;
    private String driverName;

    private String originCity;
    private String destinationCity;
    private BigDecimal availableCapacityKg;
    private BigDecimal availableCapacityVolumeM3;

    private BigDecimal matchScore;
    private MatchStatus status;
    private String note;
    private LocalDateTime createdAt;


    private Double directRouteDistanceKm;
    private Double routeWithLoadDistanceKm;
    private Double deviationKm;

}