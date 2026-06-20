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
import java.util.Map;

@RestController
@MessageMapping("/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChatController {
    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/send")
    public void sendMessage(@Valid @Payload Map<String, Object> rawMessage, ChatMessageDto chatMessageDto, Principal principal) {
        System.out.println("DEBUG: Gelen recipientId: " + chatMessageDto.getRecipientId());
        System.out.println("Gelen ham veri: " + rawMessage);
        // Eğer veri geliyorsa, burada "recipientId" anahtarını kontrol edebiliriz
        if (rawMessage.get("recipientId") == null) {
            System.out.println("UYARI: recipientId boş geldi!");
        }
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
