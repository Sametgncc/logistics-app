package com.logistics.backend.controller.business;

import com.logistics.backend.entity.business.ChatMessage;
import com.logistics.backend.payload.chat.ChatMessageDto;
import com.logistics.backend.repository.business.ChatMessageRepository;
import com.logistics.backend.service.business.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MessageController {
    private final ChatMessageService chatMessageService;

    @GetMapping("/history/{user1}/{user2}")
    public ResponseEntity<List<ChatMessageDto>> getChatHistory(@PathVariable Long user1, @PathVariable Long user2) {
        List<ChatMessageDto> history = chatMessageService.getChatHistory(user1, user2);
        return ResponseEntity.ok(history);
    }
}
