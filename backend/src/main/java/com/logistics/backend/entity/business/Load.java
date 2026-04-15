package com.logistics.backend.entity.business;



import com.logistics.backend.entity.enums.LoadStatus;
import com.logistics.backend.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "loads")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Load  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @Column(nullable = false, length = 100)
    private String pickupCity;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal pickupLat;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal pickupLng;

    @Column(nullable = false, length = 100)
    private String dropoffCity;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal dropoffLat;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal dropoffLng;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal weightKg;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal volumeM3;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private LocalDateTime pickupTimeStart;

    @Column(nullable = false)
    private LocalDateTime pickupTimeEnd;

    @Column(nullable = false)
    private LocalDateTime deliveryTimeStart;

    @Column(nullable = false)
    private LocalDateTime deliveryTimeEnd;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LoadStatus status;

    @OneToMany(mappedBy = "load", cascade = CascadeType.ALL)
    @Builder.Default
    private List<LoadMatch> matches = new ArrayList<>();

    @OneToMany(mappedBy = "load", cascade = CascadeType.ALL)
    @Builder.Default
    private List<LoadStatusHistory> statusHistory = new ArrayList<>();
}