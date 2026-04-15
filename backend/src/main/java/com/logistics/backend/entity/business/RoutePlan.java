package com.logistics.backend.entity.business;


import com.logistics.backend.entity.enums.RoutePlanStatus;
import com.logistics.backend.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "route_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoutePlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "driver_id", nullable = false)
    private User driver;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Column(nullable = false, length = 100)
    private String originCity;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal originLat;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal originLng;

    @Column(nullable = false, length = 100)
    private String destinationCity;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal destinationLat;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal destinationLng;

    @Column(nullable = false)
    private LocalDateTime departureTime;

    private LocalDateTime estimatedArrivalTime;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal availableCapacityKg;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal availableCapacityVolumeM3;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RoutePlanStatus status;

    @Column(length = 500)
    private String notes;

    @OneToMany(mappedBy = "routePlan", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("stopOrder ASC")
    @Builder.Default
    private List<RoutePlanStop> stops = new ArrayList<>();

    @OneToMany(mappedBy = "routePlan", cascade = CascadeType.ALL)
    @Builder.Default
    private List<LoadMatch> matches = new ArrayList<>();
}