package com.logistics.backend.mapper;

import com.logistics.backend.entity.user.DriverDocument;
import com.logistics.backend.payload.response.DriverDocumentResponse;
import org.springframework.stereotype.Component;

@Component
public class DriverDocumentMapper {

    public DriverDocumentResponse toResponse(DriverDocument document) {
        if (document == null) {
            return null;
        }

        return DriverDocumentResponse.builder()
                .id(document.getId())
                .driverId(document.getDriver().getId())
                .driverName(document.getDriver().getName())
                .driverEmail(document.getDriver().getEmail())
                .documentType(document.getDocumentType())
                .fileUrl(document.getFileUrl())
                .description(document.getDescription())
                .status(document.getStatus())
                .rejectionReason(document.getRejectionReason())
                .uploadedAt(document.getUploadedAt())
                .reviewedAt(document.getReviewedAt())
                .build();
    }
}