package com.logistics.backend.mapper;

import com.logistics.backend.entity.business.Load;
import com.logistics.backend.payload.response.LoadResponse;
import org.springframework.stereotype.Component;

@Component
public class LoadMapper {

    public LoadResponse toResponse(Load load) {
        if (load == null) {
            return null;
        }

        return LoadResponse.builder()
                .id(load.getId())
                .customerId(load.getCustomer().getId())
                .customerName(load.getCustomer().getName())
                .customerEmail(load.getCustomer().getEmail())
                .pickupCity(load.getPickupCity())
                .pickupLat(load.getPickupLat())
                .pickupLng(load.getPickupLng())
                .deliveryCity(load.getDeliveryCity())
                .deliveryLat(load.getDeliveryLat())
                .deliveryLng(load.getDeliveryLng())
                .weightKg(load.getWeightKg())
                .volumeM3(load.getVolumeM3())
                .pickupTime(load.getPickupTime())
                .deliveryDeadline(load.getDeliveryDeadline())
                .description(load.getDescription())
                .status(load.getStatus())
                .createdAt(load.getCreatedAt())
                .build();
    }
}