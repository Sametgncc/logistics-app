package com.logistics.backend.service.business;

import com.logistics.backend.entity.enums.DriverDocumentStatus;
import com.logistics.backend.entity.enums.Role;
import com.logistics.backend.entity.user.DriverDocument;
import com.logistics.backend.entity.user.User;
import com.logistics.backend.exception.BadRequestException;
import com.logistics.backend.exception.ResourceNotFoundException;
import com.logistics.backend.mapper.DriverDocumentMapper;
import com.logistics.backend.payload.request.CreateDriverDocumentRequest;
import com.logistics.backend.payload.request.RejectDriverDocumentRequest;
import com.logistics.backend.payload.response.DriverDocumentResponse;
import com.logistics.backend.repository.business.DriverDocumentRepository;
import com.logistics.backend.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DriverDocumentService {

    private final DriverDocumentRepository driverDocumentRepository;
    private final UserRepository userRepository;
    private final DriverDocumentMapper driverDocumentMapper;

    public DriverDocumentResponse uploadDocument(CreateDriverDocumentRequest request) {
        User driver = userRepository.findById(request.getDriverId())
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with id: " + request.getDriverId()));

        validateDriver(driver);
        validateDocumentDoesNotExist(request);

        DriverDocument document = DriverDocument.builder()
                .driver(driver)
                .documentType(request.getDocumentType())
                .fileUrl(request.getFileUrl())
                .description(request.getDescription())
                .status(DriverDocumentStatus.PENDING)
                .uploadedAt(LocalDateTime.now())
                .build();

        return driverDocumentMapper.toResponse(driverDocumentRepository.save(document));
    }

    public List<DriverDocumentResponse> getDocumentsByDriverId(Long driverId) {
        userRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with id: " + driverId));

        return driverDocumentRepository.findByDriverId(driverId)
                .stream()
                .map(driverDocumentMapper::toResponse)
                .toList();
    }

    public List<DriverDocumentResponse> getPendingDocuments() {
        return driverDocumentRepository.findByStatus(DriverDocumentStatus.PENDING)
                .stream()
                .map(driverDocumentMapper::toResponse)
                .toList();
    }

    public DriverDocumentResponse approveDocument(Long documentId) {
        DriverDocument document = findDocumentById(documentId);

        if (document.getStatus() == DriverDocumentStatus.APPROVED) {
            throw new BadRequestException("Document is already approved");
        }

        document.setStatus(DriverDocumentStatus.APPROVED);
        document.setRejectionReason(null);
        document.setReviewedAt(LocalDateTime.now());

        return driverDocumentMapper.toResponse(driverDocumentRepository.save(document));
    }

    public DriverDocumentResponse rejectDocument(Long documentId, RejectDriverDocumentRequest request) {
        DriverDocument document = findDocumentById(documentId);

        if (document.getStatus() == DriverDocumentStatus.REJECTED) {
            throw new BadRequestException("Document is already rejected");
        }

        document.setStatus(DriverDocumentStatus.REJECTED);
        document.setRejectionReason(request.getRejectionReason());
        document.setReviewedAt(LocalDateTime.now());

        return driverDocumentMapper.toResponse(driverDocumentRepository.save(document));
    }

    public DriverDocumentResponse getDocumentById(Long documentId) {
        return driverDocumentMapper.toResponse(findDocumentById(documentId));
    }

    private DriverDocument findDocumentById(Long documentId) {
        return driverDocumentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));
    }

    private void validateDriver(User driver) {
        if (Boolean.FALSE.equals(driver.getEnabled())) {
            throw new BadRequestException("Driver account is disabled");
        }
    }

    private void validateDocumentDoesNotExist(CreateDriverDocumentRequest request) {
        boolean exists = driverDocumentRepository.existsByDriverIdAndDocumentType(
                request.getDriverId(),
                request.getDocumentType()
        );

        if (exists) {
            throw new BadRequestException("This document type has already been uploaded by this driver");
        }
    }
}