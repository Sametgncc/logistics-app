package com.logistics.backend.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RejectDriverDocumentRequest {

    @NotBlank(message = "Rejection reason cannot be blank")
    @Size(max = 500, message = "Rejection reason cannot exceed 500 characters")
    private String rejectionReason;
}