package com.logistics.backend.controller.business;

import com.logistics.backend.payload.request.CreateVehicleRequest;
import com.logistics.backend.payload.request.UpdateVehicleRequest;
import com.logistics.backend.payload.response.VehicleResponse;
import com.logistics.backend.service.business.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @PreAuthorize("hasAuthority('ADMIN') or (authentication.principal.driverApproved == true)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VehicleResponse createVehicle(@Valid @RequestBody CreateVehicleRequest request) {
        return vehicleService.createVehicle(request);
    }

    @PreAuthorize("hasAuthority('ADMIN') or (authentication.principal.driverApproved == true and #ownerId == authentication.principal.id)")
    @GetMapping("/my")
    public List<VehicleResponse> getMyVehicles(@RequestParam Long ownerId) {
        return vehicleService.getVehiclesByOwnerId(ownerId);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public VehicleResponse getVehicleById(@PathVariable Long id) {
        return vehicleService.getVehicleById(id);
    }

    @PreAuthorize("hasAuthority('ADMIN') or (authentication.principal.driverApproved == true)")
    @GetMapping
    public List<VehicleResponse> getAllVehicles() {
        return vehicleService.getAllVehicles();
    }

    @PreAuthorize("hasAuthority('ADMIN') or (authentication.principal.driverApproved == true)")
    @PutMapping("/{id}")
    public VehicleResponse updateVehicle(
            @PathVariable Long id,
            @Valid @RequestBody UpdateVehicleRequest request
    ) {
        return vehicleService.updateVehicle(id, request);
    }

    @PreAuthorize("hasAuthority('ADMIN') or (authentication.principal.driverApproved == true)")
    @PatchMapping("/{id}/deactivate")
    public VehicleResponse deactivateVehicle(@PathVariable Long id) {
        return vehicleService.deactivateVehicle(id);
    }

    @PreAuthorize("hasAuthority('ADMIN') or (authentication.principal.driverApproved == true)")
    @PatchMapping("/{id}/activate")
    public VehicleResponse activateVehicle(@PathVariable Long id) {
        return vehicleService.activateVehicle(id);
    }
}