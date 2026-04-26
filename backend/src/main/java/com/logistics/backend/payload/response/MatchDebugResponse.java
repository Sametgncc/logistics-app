package com.logistics.backend.payload.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MatchDebugResponse {

    private Long loadId;
    private Long routePlanId;

    private boolean routePlanOpen;
    private boolean enoughCapacity;
    private boolean cityCompatible;
    private boolean timeCompatible;
    private boolean acceptableDeviation;

    private Double directRouteDistanceKm;
    private Double routeWithLoadDistanceKm;
    private Double deviationKm;

    private boolean suitable;
    private String reason;



}