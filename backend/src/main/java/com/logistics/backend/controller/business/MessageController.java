package com.logistics.backend.controller.business;

import com.logistics.backend.exception.BadRequestException;
import com.logistics.backend.payload.chat.ChatMessageDto;
import com.logistics.backend.security.service.UserDetailsImpl;
import com.logistics.backend.service.business.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MessageController {
    private final ChatMessageService chatMessageService;

    @GetMapping("/history/{otherUserId}")
    public ResponseEntity<List<ChatMessageDto>> getChatHistory(
            @PathVariable Long otherUserId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        List<ChatMessageDto> history = chatMessageService.getChatHistory(currentUserId(userDetails), otherUserId);
        return ResponseEntity.ok(history);
    }

    /** Existing clients can keep the former URL without exposing other users' chats. */
    @GetMapping("/history/{user1}/{user2}")
    public ResponseEntity<List<ChatMessageDto>> getLegacyChatHistory(
            @PathVariable Long user1,
            @PathVariable Long user2,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long currentUserId = currentUserId(userDetails);
        if (!currentUserId.equals(user1) && !currentUserId.equals(user2)) {
            throw new BadRequestException("Sadece kendi sohbet geçmişinizi görüntüleyebilirsiniz");
        }

        Long otherUserId = currentUserId.equals(user1) ? user2 : user1;
        return ResponseEntity.ok(chatMessageService.getChatHistory(currentUserId, otherUserId));
    }

    private Long currentUserId(UserDetailsImpl userDetails) {
        if (userDetails == null || userDetails.getId() == null) {
            throw new BadRequestException("Kimlik doğrulaması gerekli");
        }
        return userDetails.getId();
    }
}
