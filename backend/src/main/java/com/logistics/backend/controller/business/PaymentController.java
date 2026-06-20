package com.logistics.backend.controller.business;

import com.logistics.backend.payload.request.CreateCheckoutPaymentRequest;
import com.logistics.backend.payload.response.CheckoutPaymentResponse;
import jakarta.validation.Valid;
import com.logistics.backend.security.service.UserDetailsImpl;
import com.logistics.backend.service.business.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/iyzico/callback")
    public CheckoutPaymentResponse iyzicoCallback(@RequestParam String token) {
        return paymentService.verifyCheckout(token);
    }
}
