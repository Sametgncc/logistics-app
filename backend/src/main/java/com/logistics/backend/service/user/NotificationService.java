package com.logistics.backend.service.user;

import com.logistics.backend.entity.enums.NotificationType;
import com.logistics.backend.entity.user.Notification;
import com.logistics.backend.entity.user.User;
import com.logistics.backend.exception.BadRequestException;
import com.logistics.backend.exception.ResourceNotFoundException;
import com.logistics.backend.mapper.NotificationMapper;
import com.logistics.backend.payload.response.NotificationResponse;
import com.logistics.backend.repository.user.NotificationRepository;
import com.logistics.backend.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;

    public NotificationResponse createNotification(
            Long userId,
            NotificationType type,
            String title,
            String message,
            String referenceType,
            Long referenceId
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();

        return notificationMapper.toResponse(notificationRepository.save(notification));
    }

    public List<NotificationResponse> getMyNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    public NotificationResponse markAsRead(Long notificationId, Long userId) {
        Notification notification = findOwnedNotification(notificationId, userId);

        if (Boolean.TRUE.equals(notification.getRead())) {
            return notificationMapper.toResponse(notification);
        }

        notification.setRead(true);
        notification.setReadAt(LocalDateTime.now());

        return notificationMapper.toResponse(notificationRepository.save(notification));
    }

    private Notification findOwnedNotification(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));

        if (!notification.getUser().getId().equals(userId)) {
            throw new BadRequestException("Notification does not belong to current user");
        }

        return notification;
    }
}
