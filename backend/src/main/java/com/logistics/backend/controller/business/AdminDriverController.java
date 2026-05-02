package com.logistics.backend.controller.business;

import com.logistics.backend.payload.response.UserResponse;
import com.logistics.backend.service.business.DriverApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/drivers")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN')")
public class AdminDriverController {

    private final DriverApprovalService driverApprovalService;

    @PatchMapping("/{driverId}/approve")
    public UserResponse approveDriver(@PathVariable Long driverId) {
        return driverApprovalService.approveDriver(driverId);
    }

    @PatchMapping("/{driverId}/reject")
    public UserResponse rejectDriver(@PathVariable Long driverId) {
        return driverApprovalService.rejectDriver(driverId);
    }
}