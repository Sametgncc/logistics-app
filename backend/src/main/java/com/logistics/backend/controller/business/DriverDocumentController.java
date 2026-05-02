package com.logistics.backend.controller.business;

import com.logistics.backend.payload.request.CreateDriverDocumentRequest;
import com.logistics.backend.payload.request.RejectDriverDocumentRequest;
import com.logistics.backend.payload.response.DriverDocumentResponse;
import com.logistics.backend.service.business.DriverDocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/driver-documents")
@RequiredArgsConstructor
public class DriverDocumentController {

    private final DriverDocumentService driverDocumentService;

    @PreAuthorize("hasAnyAuthority('DRIVER', 'ROLE_DRIVER')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DriverDocumentResponse uploadDocument(
            @Valid @RequestBody CreateDriverDocumentRequest request
    ) {
        return driverDocumentService.uploadDocument(request);
    }

    @PreAuthorize("hasAnyAuthority('DRIVER', 'ROLE_DRIVER', 'ADMIN', 'ROLE_ADMIN')")
    @GetMapping("/driver/{driverId}")
    public List<DriverDocumentResponse> getDocumentsByDriverId(@PathVariable Long driverId) {
        return driverDocumentService.getDocumentsByDriverId(driverId);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN')")
    @GetMapping("/pending")
    public List<DriverDocumentResponse> getPendingDocuments() {
        return driverDocumentService.getPendingDocuments();
    }

    @PreAuthorize("hasAnyAuthority('DRIVER', 'ROLE_DRIVER', 'ADMIN', 'ROLE_ADMIN')")
    @GetMapping("/{documentId}")
    public DriverDocumentResponse getDocumentById(@PathVariable Long documentId) {
        return driverDocumentService.getDocumentById(documentId);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN')")
    @PatchMapping("/{documentId}/approve")
    public DriverDocumentResponse approveDocument(@PathVariable Long documentId) {
        return driverDocumentService.approveDocument(documentId);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN')")
    @PatchMapping("/{documentId}/reject")
    public DriverDocumentResponse rejectDocument(
            @PathVariable Long documentId,
            @Valid @RequestBody RejectDriverDocumentRequest request
    ) {
        return driverDocumentService.rejectDocument(documentId, request);
    }
}