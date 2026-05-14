package com.logistics.backend.payload.response;

import com.logistics.backend.entity.enums.NotificationType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {

    private Long id;
    private Long userId;
    private NotificationType type;
    private String title;
    private String message;
    private String referenceType;
    private Long referenceId;
    private Boolean read;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}
