package com.logistics.backend.controller.business;


import com.logistics.backend.payload.request.CreateRoutePlanRequest;
import com.logistics.backend.payload.request.UpdateRoutePlanRequest;
import com.logistics.backend.payload.request.UpdateDriverLocationRequest;
import com.logistics.backend.payload.response.DriverLocationResponse;
import com.logistics.backend.payload.response.RoutePlanResponse;
import com.logistics.backend.security.service.UserDetailsImpl;
import com.logistics.backend.service.business.DriverTrackingService;
import com.logistics.backend.service.business.RoutePlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/route-plans")
@RequiredArgsConstructor
public class RoutePlanController {

    private final RoutePlanService routePlanService;
    private final DriverTrackingService driverTrackingService;


    @PreAuthorize("(authentication.principal.driverApproved == true)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoutePlanResponse createRoutePlan(@Valid @RequestBody CreateRoutePlanRequest request) {
        return routePlanService.createRoutePlan(request);
    }

    @GetMapping("/my")
    public List<RoutePlanResponse> getMyRoutePlans(@RequestParam Long driverId) {
        return routePlanService.getMyRoutePlans(driverId);
    }

    @GetMapping("/{id}")
    public RoutePlanResponse getRoutePlanById(@PathVariable Long id) {
        return routePlanService.getRoutePlanById(id);
    }

    @PostMapping("/{routePlanId}/location")
    @PreAuthorize("hasAuthority('DRIVER') or authentication.principal.driverApproved == true")
    public DriverLocationResponse updateDriverLocation(
            @PathVariable Long routePlanId,
            @Valid @RequestBody UpdateDriverLocationRequest request,
            @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        return driverTrackingService.recordLocation(routePlanId, currentUser.getId(), request);
    }

    @GetMapping
    public List<RoutePlanResponse> getOpenRoutePlans(@RequestParam(required = false) String status) {
        if (status == null || "OPEN".equalsIgnoreCase(status)) {
            return routePlanService.getOpenRoutePlans();
        }

        return routePlanService.getOpenRoutePlans();
    }

    @PutMapping("/{id}")
    public RoutePlanResponse updateRoutePlan(@PathVariable Long id,
                                             @Valid @RequestBody UpdateRoutePlanRequest request) {
        return routePlanService.updateRoutePlan(id, request);
    }

    @PatchMapping("/{id}/cancel")
    public RoutePlanResponse cancelRoutePlan(@PathVariable Long id) {
        return routePlanService.cancelRoutePlan(id);
    }
}
