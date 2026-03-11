package com.logistics.backend.entity.business;

import com.logistics.backend.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RoutePlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String originCity;

    private String destinationCity;

    private LocalDateTime departureTime;

    private double totalCapacity;

    private double remainingCapacity;

    @OneToOne(fetch = FetchType.EAGER)
    private User driver;
}