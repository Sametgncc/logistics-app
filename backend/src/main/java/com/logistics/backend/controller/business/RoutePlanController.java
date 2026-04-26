package com.logistics.backend.controller.business;


import com.logistics.backend.payload.request.CreateRoutePlanRequest;
import com.logistics.backend.payload.request.UpdateRoutePlanRequest;
import com.logistics.backend.payload.response.RoutePlanResponse;
import com.logistics.backend.service.business.RoutePlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/route-plans")
@RequiredArgsConstructor
public class RoutePlanController {

    private final RoutePlanService routePlanService;


    @PreAuthorize("hasAuthority('DRIVER')")
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