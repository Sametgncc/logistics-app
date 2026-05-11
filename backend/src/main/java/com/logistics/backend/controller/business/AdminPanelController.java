package com.logistics.backend.controller.business;

import com.logistics.backend.payload.response.AdminDashboardResponse;
import com.logistics.backend.payload.response.LoadResponse;
import com.logistics.backend.payload.response.RoutePlanResponse;
import com.logistics.backend.payload.response.TransportOfferResponse;
import com.logistics.backend.payload.response.UserResponse;
import com.logistics.backend.service.business.AdminPanelService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN')")
public class AdminPanelController {

    private final AdminPanelService adminPanelService;

    @GetMapping("/dashboard")
    public AdminDashboardResponse getDashboard() {
        return adminPanelService.getDashboard();
    }

    @GetMapping("/users")
    public List<UserResponse> getUsers(@RequestParam(required = false) String role) {
        return adminPanelService.getUsers(role);
    }

    @PatchMapping("/users/{userId}/deactivate")
    public UserResponse deactivateUser(@PathVariable Long userId) {
        return adminPanelService.deactivateUser(userId);
    }

    @PatchMapping("/users/{userId}/activate")
    public UserResponse activateUser(@PathVariable Long userId) {
        return adminPanelService.activateUser(userId);
    }

    @GetMapping("/loads")
    public List<LoadResponse> getLoads(@RequestParam(required = false) String status) {
        return adminPanelService.getLoads(status);
    }

    @GetMapping("/route-plans")
    public List<RoutePlanResponse> getRoutePlans(@RequestParam(required = false) String status) {
        return adminPanelService.getRoutePlans(status);
    }

    @GetMapping("/transport-offers")
    public List<TransportOfferResponse> getTransportOffers(@RequestParam(required = false) String status) {
        return adminPanelService.getTransportOffers(status);
    }
}
