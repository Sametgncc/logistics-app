package com.logistics.backend.mapper;

import com.logistics.backend.entity.business.Load;
import com.logistics.backend.entity.business.LoadMatch;
import com.logistics.backend.entity.business.RoutePlan;
import com.logistics.backend.payload.response.LoadMatchResponse;
import org.springframework.stereotype.Component;

@Component
public class LoadMatchMapper {

    public LoadMatchResponse toResponse(LoadMatch loadMatch) {
        if (loadMatch == null) {
            return null;
        }

        Load load = loadMatch.getLoad();
        RoutePlan routePlan = loadMatch.getRoutePlan();

        return LoadMatchResponse.builder()
                .id(loadMatch.getId())

                .loadId(load.getId())
                .pickupCity(load.getPickupCity())
                .pickupLat(load.getPickupLat())
                .pickupLng(load.getPickupLng())
                .deliveryCity(load.getDeliveryCity())
                .deliveryLat(load.getDeliveryLat())
                .deliveryLng(load.getDeliveryLng())
                .weightKg(load.getWeightKg())
                .volumeM3(load.getVolumeM3())

                .routePlanId(routePlan.getId())
                .driverId(routePlan.getDriver().getId())
                .driverName(routePlan.getDriver().getName())

                .originCity(routePlan.getOriginCity())
                .destinationCity(routePlan.getDestinationCity())
                .availableCapacityKg(routePlan.getAvailableCapacityKg())
                .availableCapacityVolumeM3(routePlan.getAvailableCapacityVolumeM3())

                .matchScore(loadMatch.getMatchScore())
                .status(loadMatch.getStatus())
                .note(loadMatch.getNote())
                .createdAt(loadMatch.getCreatedAt())

                .directRouteDistanceKm(loadMatch.getDirectRouteDistanceKm() != null
                        ? loadMatch.getDirectRouteDistanceKm().doubleValue()
                        : null)
                .routeWithLoadDistanceKm(loadMatch.getRouteWithLoadDistanceKm() != null
                        ? loadMatch.getRouteWithLoadDistanceKm().doubleValue()
                        : null)
                .deviationKm(loadMatch.getDeviationKm() != null
                        ? loadMatch.getDeviationKm().doubleValue()
                        : null)
                .build();
    }
}
