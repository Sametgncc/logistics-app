package com.logistics.backend.payload.request;

import com.logistics.backend.entity.enums.VehicleType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateVehicleRequest {

    @NotBlank(message = "Plate number cannot be blank")
    @Size(max = 20, message = "Plate number cannot exceed 20 characters")
    private String plateNumber;

    @NotNull(message = "Vehicle type cannot be null")
    private VehicleType vehicleType;

    @NotNull(message = "Capacity kg cannot be null")
    @DecimalMin(value = "0.01", message = "Capacity kg must be greater than zero")
    private BigDecimal capacityKg;

    @NotNull(message = "Capacity volume cannot be null")
    @DecimalMin(value = "0.01", message = "Capacity volume must be greater than zero")
    private BigDecimal capacityVolumeM3;

    @NotNull(message = "Active status cannot be null")
    private Boolean active;
}