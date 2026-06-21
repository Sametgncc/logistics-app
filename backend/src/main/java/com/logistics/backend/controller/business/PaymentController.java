package com.logistics.backend.controller.business;

import com.logistics.backend.entity.enums.PaymentStatus;
import com.logistics.backend.payload.request.CreateCheckoutPaymentRequest;
import com.logistics.backend.payload.response.CheckoutPaymentResponse;
import jakarta.validation.Valid;
import com.logistics.backend.security.service.UserDetailsImpl;
import com.logistics.backend.service.business.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/checkout")
    public CheckoutPaymentResponse startCheckout(
            @Valid @RequestBody CreateCheckoutPaymentRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return paymentService.startCheckout(request, userDetails.getId());
    }

    @PostMapping(value = "/iyzico/callback", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public RedirectView iyzicoCallback(@RequestParam String token) {
        CheckoutPaymentResponse response = paymentService.verifyCheckout(token);

        String frontendBaseUrl = "http://localhost:5000";

        if (response.getStatus() == PaymentStatus.PAID) {
            return new RedirectView(frontendBaseUrl + "/payment-success?token=" + token);
        } else {
            return new RedirectView(frontendBaseUrl + "/payment-failed");
        }
    }
}
