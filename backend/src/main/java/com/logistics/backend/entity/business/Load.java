package com.logistics.backend.entity.business;

import com.logistics.backend.entity.enums.LoadStatus;
import com.logistics.backend.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "loads")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Load {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // İlanı veren müşteri
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @Column(nullable = false, length = 100)
    private String pickupCity;

    @Column(nullable = false)
    private BigDecimal pickupLat;

    @Column(nullable = false)
    private BigDecimal pickupLng;

    @Column(nullable = false, length = 100)
    private String deliveryCity;

    @Column(nullable = false)
    private BigDecimal deliveryLat;

    @Column(nullable = false)
    private BigDecimal deliveryLng;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal weightKg;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal volumeM3;

    @Column(nullable = false)
    private LocalDateTime pickupTime;

    private LocalDateTime deliveryDeadline;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LoadStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}