package com.logistics.backend.mapper;

import com.logistics.backend.entity.business.RoutePlan;
import com.logistics.backend.payload.response.RoutePlanResponse;
import org.springframework.stereotype.Component;

@Component
public class RoutePlanMapper {

    public RoutePlanResponse toResponse(RoutePlan routePlan) {
        return RoutePlanResponse.builder()
                .id(routePlan.getId())
                .driverId(routePlan.getDriver().getId())
                .driverFullName(routePlan.getDriver().getName() + " " + routePlan.getDriver().getLastName())
                .vehicleId(routePlan.getVehicle().getId())
                .plateNumber(routePlan.getVehicle().getPlateNumber())
                .originCity(routePlan.getOriginCity())
                .originLat(routePlan.getOriginLat())
                .originLng(routePlan.getOriginLng())
                .destinationCity(routePlan.getDestinationCity())
                .destinationLat(routePlan.getDestinationLat())
                .destinationLng(routePlan.getDestinationLng())
                .departureTime(routePlan.getDepartureTime())
                .estimatedArrivalTime(routePlan.getEstimatedArrivalTime())
                .availableCapacityKg(routePlan.getAvailableCapacityKg())
                .availableCapacityVolumeM3(routePlan.getAvailableCapacityVolumeM3())
                .status(routePlan.getStatus())
                .notes(routePlan.getNotes())
                .build();
    }
}