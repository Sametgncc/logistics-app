package com.logistics.backend.mapper;

import com.logistics.backend.entity.business.Vehicle;
import com.logistics.backend.payload.response.VehicleResponse;
import org.springframework.stereotype.Component;

@Component
public class VehicleMapper {

    public VehicleResponse toResponse(Vehicle vehicle) {
        if (vehicle == null) {
            return null;
        }

        return VehicleResponse.builder()
                .id(vehicle.getId())
                .ownerId(vehicle.getOwner().getId())
                .ownerName(vehicle.getOwner().getName())
                .ownerEmail(vehicle.getOwner().getEmail())
                .plateNumber(vehicle.getPlateNumber())
                .vehicleType(vehicle.getVehicleType())
                .capacityKg(vehicle.getCapacityKg())
                .capacityVolumeM3(vehicle.getCapacityVolumeM3())
                .active(vehicle.getActive())
                .build();
    }
}