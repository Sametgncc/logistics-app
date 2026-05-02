package com.logistics.backend.payload.response;

import com.logistics.backend.entity.enums.DriverDocumentStatus;
import com.logistics.backend.entity.enums.DriverDocumentType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DriverDocumentResponse {

    private Long id;

    private Long driverId;
    private String driverName;
    private String driverEmail;

    private DriverDocumentType documentType;
    private String fileUrl;
    private String description;

    private DriverDocumentStatus status;
    private String rejectionReason;

    private LocalDateTime uploadedAt;
    private LocalDateTime reviewedAt;
}