package com.logistics.backend.controller.business;

import com.logistics.backend.payload.chat.ChatMessageDto;
import com.logistics.backend.service.business.ChatMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@MessageMapping("/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChatController {
    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/send")
    public void sendMessage(@Valid @Payload ChatMessageDto chatMessageDto, Principal principal) {
        if (principal == null) {
            throw new AccessDeniedException("WebSocket bağlantısı doğrulanmadı");
        }

        ChatMessageDto savedMsg = chatMessageService.saveMessage(
                Long.valueOf(principal.getName()),
                chatMessageDto
        );

        // 2. Mesajı Alıcıya Gönder (DTO olarak gidiyor)
        messagingTemplate.convertAndSendToUser(
                String.valueOf(savedMsg.getRecipientId()),
                "/queue/messages",
                savedMsg
        );
    }
}
