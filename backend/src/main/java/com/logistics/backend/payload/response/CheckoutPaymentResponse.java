package com.logistics.backend.payload.response;

import com.logistics.backend.entity.enums.PaymentStatus;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class CheckoutPaymentResponse {
    Long paymentId;
    PaymentStatus status;
    BigDecimal amount;
    String currency;
    String paymentPageUrl;
}
