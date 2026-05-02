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

    @PreAuthorize("hasAnyAuthority('DRIVER', 'ROLE_DRIVER')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VehicleResponse createVehicle(@Valid @RequestBody CreateVehicleRequest request) {
        return vehicleService.createVehicle(request);
    }

    @PreAuthorize("hasAnyAuthority('DRIVER', 'ROLE_DRIVER')")
    @GetMapping("/my")
    public List<VehicleResponse> getMyVehicles(@RequestParam Long ownerId) {
        return vehicleService.getVehiclesByOwnerId(ownerId);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public VehicleResponse getVehicleById(@PathVariable Long id) {
        return vehicleService.getVehicleById(id);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN', 'DRIVER', 'ROLE_DRIVER')")
    @GetMapping
    public List<VehicleResponse> getAllVehicles() {
        return vehicleService.getAllVehicles();
    }

    @PreAuthorize("hasAnyAuthority('DRIVER', 'ROLE_DRIVER')")
    @PutMapping("/{id}")
    public VehicleResponse updateVehicle(
            @PathVariable Long id,
            @Valid @RequestBody UpdateVehicleRequest request
    ) {
        return vehicleService.updateVehicle(id, request);
    }

    @PreAuthorize("hasAnyAuthority('DRIVER', 'ROLE_DRIVER', 'ADMIN', 'ROLE_ADMIN')")
    @PatchMapping("/{id}/deactivate")
    public VehicleResponse deactivateVehicle(@PathVariable Long id) {
        return vehicleService.deactivateVehicle(id);
    }

    @PreAuthorize("hasAnyAuthority('DRIVER', 'ROLE_DRIVER', 'ADMIN', 'ROLE_ADMIN')")
    @PatchMapping("/{id}/activate")
    public VehicleResponse activateVehicle(@PathVariable Long id) {
        return vehicleService.activateVehicle(id);
    }
}
