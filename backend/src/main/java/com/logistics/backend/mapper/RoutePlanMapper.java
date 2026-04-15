package com.logistics.backend.mapper;

import com.logistics.backend.entity.business.RoutePlan;
import org.springframework.stereotype.Component;

@Component
public class RoutePlanMapper {

    public void updateRoutePlanFromDto(RoutePlan existingRoutePlan, RoutePlan updatedRoutePlan) {
        existingRoutePlan.setDriver(updatedRoutePlan.getDriver());
        existingRoutePlan.setVehicle(updatedRoutePlan.getVehicle());
        existingRoutePlan.setOriginCity(updatedRoutePlan.getOriginCity());
        existingRoutePlan.setOriginLat(updatedRoutePlan.getOriginLat());
        existingRoutePlan.setOriginLng(updatedRoutePlan.getOriginLng());
        existingRoutePlan.setDestinationCity(updatedRoutePlan.getDestinationCity());
        existingRoutePlan.setDestinationLat(updatedRoutePlan.getDestinationLat());
        existingRoutePlan.setDestinationLng(updatedRoutePlan.getDestinationLng());
        existingRoutePlan.setDepartureTime(updatedRoutePlan.getDepartureTime());
        existingRoutePlan.setEstimatedArrivalTime(updatedRoutePlan.getEstimatedArrivalTime());
        existingRoutePlan.setAvailableCapacityKg(updatedRoutePlan.getAvailableCapacityKg());
        existingRoutePlan.setAvailableCapacityVolumeM3(updatedRoutePlan.getAvailableCapacityVolumeM3());
        existingRoutePlan.setStatus(updatedRoutePlan.getStatus());
        existingRoutePlan.setNotes(updatedRoutePlan.getNotes());
    }
}
