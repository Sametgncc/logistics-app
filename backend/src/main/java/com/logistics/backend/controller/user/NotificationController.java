package com.logistics.backend.controller.user;

import com.logistics.backend.payload.response.NotificationResponse;
import com.logistics.backend.security.service.UserDetailsImpl;
import com.logistics.backend.service.user.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/my")
    public List<NotificationResponse> getMyNotifications(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return notificationService.getMyNotifications(userDetails.getId());
    }

    @GetMapping("/unread-count")
    public Map<String, Long> getUnreadCount(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return Map.of("unreadCount", notificationService.getUnreadCount(userDetails.getId()));
    }

    @PatchMapping("/{notificationId}/read")
    public NotificationResponse markAsRead(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return notificationService.markAsRead(notificationId, userDetails.getId());
    }
}
