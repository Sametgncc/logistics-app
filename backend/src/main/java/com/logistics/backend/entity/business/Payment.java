package com.logistics.backend.entity.business;

import com.logistics.backend.entity.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments", uniqueConstraints = {
        @UniqueConstraint(name = "uk_payment_offer", columnNames = "transport_offer_id"),
        @UniqueConstraint(name = "uk_payment_iyzico_token", columnNames = "iyzico_token")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transport_offer_id", nullable = false)
    private TransportOffer transportOffer;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "iyzico_token", length = 100)
    private String iyzicoToken;

    @Column(name = "iyzico_payment_id", length = 100)
    private String iyzicoPaymentId;

    @Column(nullable = false, unique = true, length = 64)
    private String conversationId;

    @Column(length = 500)
    private String failureReason;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime completedAt;
}
