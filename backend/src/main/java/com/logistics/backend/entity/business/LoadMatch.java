package com.logistics.backend.entity.business;

import com.logistics.backend.entity.enums.MatchStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "load_matches",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_route_plan_load_match", columnNames = {"route_plan_id", "load_id"})
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "route_plan_id", nullable = false)
    private RoutePlan routePlan;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "load_id", nullable = false)
    private Load load;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal matchScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MatchStatus status;

    private LocalDateTime matchedAt;

    @Column(nullable = false)
    private Boolean approvedByDriver = false;

    @Column(nullable = false)
    private Boolean approvedByCustomer = false;

    @OneToMany(mappedBy = "loadMatch", cascade = CascadeType.ALL)
    @Builder.Default
    private List<MatchStatusHistory> statusHistory = new ArrayList<>();
}