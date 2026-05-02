package com.logistics.backend.payload.response;

import com.logistics.backend.entity.enums.VehicleType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class VehicleResponse {

    private Long id;

    private Long ownerId;
    private String ownerName;
    private String ownerEmail;

    private String plateNumber;
    private VehicleType vehicleType;

    private BigDecimal capacityKg;
    private BigDecimal capacityVolumeM3;

    private Boolean active;
}