package com.logistics.backend.payload.request;

import com.logistics.backend.entity.enums.OfferDirection;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateTransportOfferRequest {

    @NotNull(message = "Load id cannot be null")
    private Long loadId;

    @NotNull(message = "Route plan id cannot be null")
    private Long routePlanId;

    @NotNull(message = "Offer direction cannot be null")
    private OfferDirection direction;

    @NotNull(message = "Price cannot be null")
    @DecimalMin(value = "0.01", message = "Price must be greater than zero")
    private BigDecimal price;

    @Size(max = 500, message = "Message cannot exceed 500 characters")
    private String message;
}
