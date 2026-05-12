package com.logistics.backend.payload.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RespondTransportOfferRequest {

    @Size(max = 500, message = "Response message cannot exceed 500 characters")
    private String responseMessage;
}