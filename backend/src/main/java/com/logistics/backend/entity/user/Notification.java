package com.logistics.backend.entity.user;

import com.logistics.backend.entity.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType type;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(length = 60)
    private String referenceType;

    private Long referenceId;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean read = false;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime readAt;
}
