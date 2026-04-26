package com.logistics.backend.entity.business;

import com.logistics.backend.entity.enums.MatchStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "load_matches",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_load_route_plan",
                        columnNames = {"load_id", "route_plan_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoadMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Hangi yük ilanı?
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "load_id", nullable = false)
    private Load load;

    // Hangi şoför rotası?
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "route_plan_id", nullable = false)
    private RoutePlan routePlan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MatchStatus status;

    // Basit skor: kapasite, rota, zaman uygunluğu gibi şeylerden üretilecek
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal matchScore;

    @Column(length = 500)
    private String note;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}