package com.logistics.backend.controller.business;

import com.logistics.backend.payload.chat.ChatMessageDto;
import com.logistics.backend.service.business.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/send")
    public void sendMessage(@Payload ChatMessageDto chatMessageDto) {
        ChatMessageDto savedMsg = chatMessageService.saveMessage(chatMessageDto);

        // 2. Mesajı Alıcıya Gönder (DTO olarak gidiyor)
        messagingTemplate.convertAndSendToUser(
                String.valueOf(savedMsg.getRecipientId()),
                "/queue/messages",
                savedMsg
        );
    }
}
