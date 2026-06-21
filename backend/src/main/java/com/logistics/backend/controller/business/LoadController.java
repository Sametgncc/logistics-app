package com.logistics.backend.controller.business;

import com.logistics.backend.payload.request.CreateLoadRequest;
import com.logistics.backend.payload.request.UpdateLoadRequest;
import com.logistics.backend.payload.response.LoadTrackingResponse;
import com.logistics.backend.payload.response.LoadResponse;
import com.logistics.backend.security.service.UserDetailsImpl;
import com.logistics.backend.service.business.DriverTrackingService;
import com.logistics.backend.service.business.LoadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/loads")
@RequiredArgsConstructor
public class LoadController {

    private final LoadService loadService;
    private final DriverTrackingService driverTrackingService;

    @PreAuthorize("hasAuthority('CUSTOMER')")
    @PostMapping
    public LoadResponse createLoad(@Valid @RequestBody CreateLoadRequest request) {
        return loadService.createLoad(request);
    }

    @GetMapping("/my")
    public List<LoadResponse> getMyLoads(@RequestParam Long customerId) {
        return loadService.getMyLoads(customerId);
    }

    @GetMapping("/{id}")
    public LoadResponse getLoadById(@PathVariable Long id) {
        return loadService.getLoadById(id);
    }

    @GetMapping("/{loadId}/tracking")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public LoadTrackingResponse getLiveTracking(
            @PathVariable Long loadId,
            @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        return driverTrackingService.getLatestLocationForLoad(loadId, currentUser.getId());
    }

    @GetMapping
    public List<LoadResponse> getOpenLoads() {
        return loadService.getOpenLoads();
    }

    @PutMapping("/{id}")
    public LoadResponse updateLoad(
            @PathVariable Long id,
            @Valid @RequestBody UpdateLoadRequest request
    ) {
        return loadService.updateLoad(id, request);
    }

    @PatchMapping("/{id}/cancel")
    public LoadResponse cancelLoad(@PathVariable Long id) {
        return loadService.cancelLoad(id);
    }
}
