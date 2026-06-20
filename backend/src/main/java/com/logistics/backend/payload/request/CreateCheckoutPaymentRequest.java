package com.logistics.backend.payload.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateCheckoutPaymentRequest {

    @NotNull(message = "Offer id cannot be null")
    private Long offerId;
}
