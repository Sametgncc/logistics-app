package com.logistics.backend.mapper;

import com.logistics.backend.entity.user.Notification;
import com.logistics.backend.payload.response.NotificationResponse;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationResponse toResponse(Notification notification) {
        if (notification == null) {
            return null;
        }

        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUser().getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .referenceType(notification.getReferenceType())
                .referenceId(notification.getReferenceId())
                .read(notification.getRead())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .build();
    }
}
