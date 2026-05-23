package com.logistics.backend.controller.business;

import com.logistics.backend.payload.request.CreateTransportOfferRequest;
import com.logistics.backend.payload.request.CounterTransportOfferRequest;
import com.logistics.backend.payload.response.TransportOfferResponse;
import com.logistics.backend.security.service.UserDetailsImpl;
import com.logistics.backend.service.business.TransportOfferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transport-offers")
@RequiredArgsConstructor
public class TransportOfferController {

    private final TransportOfferService transportOfferService;

    @PreAuthorize("hasAnyAuthority('DRIVER', 'ROLE_DRIVER', 'CUSTOMER', 'ROLE_CUSTOMER')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransportOfferResponse createOffer(
            @Valid @RequestBody CreateTransportOfferRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return transportOfferService.createOffer(request, userDetails.getId());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/loads/{loadId}")
    public List<TransportOfferResponse> getOffersByLoadId(@PathVariable Long loadId) {
        return transportOfferService.getOffersByLoadId(loadId);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/route-plans/{routePlanId}")
    public List<TransportOfferResponse> getOffersByRoutePlanId(@PathVariable Long routePlanId) {
        return transportOfferService.getOffersByRoutePlanId(routePlanId);
    }

    @PreAuthorize("hasAnyAuthority('DRIVER', 'ROLE_DRIVER')")
    @GetMapping("/drivers/{driverId}")
    public List<TransportOfferResponse> getOffersByDriverId(@PathVariable Long driverId) {
        return transportOfferService.getOffersByDriverId(driverId);
    }

    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'ROLE_CUSTOMER')")
    @GetMapping("/customers/{customerId}")
    public List<TransportOfferResponse> getOffersByCustomerId(@PathVariable Long customerId) {
        return transportOfferService.getOffersByCustomerId(customerId);
    }

    @PreAuthorize("hasAnyAuthority('DRIVER', 'ROLE_DRIVER', 'CUSTOMER', 'ROLE_CUSTOMER')")
    @PostMapping("/{offerId}/counter")
    @ResponseStatus(HttpStatus.CREATED)
    public TransportOfferResponse counterOffer(
            @PathVariable Long offerId,
            @Valid @RequestBody CounterTransportOfferRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return transportOfferService.counterOffer(offerId, request, userDetails.getId());
    }

    @PreAuthorize("hasAnyAuthority('DRIVER', 'ROLE_DRIVER', 'CUSTOMER', 'ROLE_CUSTOMER')")
    @PatchMapping("/{offerId}/accept")
    public TransportOfferResponse acceptOffer(@PathVariable Long offerId) {
        return transportOfferService.acceptOffer(offerId);
    }

    @PreAuthorize("hasAnyAuthority('DRIVER', 'ROLE_DRIVER', 'CUSTOMER', 'ROLE_CUSTOMER')")
    @PatchMapping("/{offerId}/reject")
    public TransportOfferResponse rejectOffer(@PathVariable Long offerId) {
        return transportOfferService.rejectOffer(offerId);
    }

    @PreAuthorize("hasAnyAuthority('DRIVER', 'ROLE_DRIVER', 'CUSTOMER', 'ROLE_CUSTOMER')")
    @PatchMapping("/{offerId}/cancel")
    public TransportOfferResponse cancelOffer(@PathVariable Long offerId) {
        return transportOfferService.cancelOffer(offerId);
    }
}
