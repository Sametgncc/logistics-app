package com.logistics.backend.payload.request;

import com.logistics.backend.entity.enums.DriverDocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateDriverDocumentRequest {

    @NotNull(message = "Driver id cannot be null")
    private Long driverId;

    @NotNull(message = "Document type cannot be null")
    private DriverDocumentType documentType;

    @NotBlank(message = "File URL cannot be blank")
    private String fileUrl;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;
}