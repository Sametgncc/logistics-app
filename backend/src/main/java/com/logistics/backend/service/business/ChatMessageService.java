package com.logistics.backend.service.business;

import com.logistics.backend.entity.business.ChatMessage;
import com.logistics.backend.entity.user.User;
import com.logistics.backend.mapper.ChatMessageMapper;
import com.logistics.backend.payload.chat.ChatMessageDto;
import com.logistics.backend.repository.business.ChatMessageRepository;
import com.logistics.backend.repository.user.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChatMessageMapper messageMapper;

    @Transactional
    public ChatMessageDto saveMessage(ChatMessageDto messageDto) {
        System.out.println("GELEN MESAJ: " + messageDto.getContent());
        if (messageDto.getTimestamp() == null) {
            messageDto.setTimestamp(LocalDateTime.now());
        }

        User sender = userRepository.findById(messageDto.getSenderId())
                .orElseThrow(() -> new RuntimeException("Gönderen kullanıcı bulunamadı"));

        User receiver = userRepository.findById(messageDto.getRecipientId())
                .orElseThrow(() -> new RuntimeException("Alıcı kullanıcı bulunamadı"));

        // Opsiyonel: Nakliyeciniz projesi için Rol Kontrolü
        // (Örn: Sadece Müşteri ve Sürücü mesajlaşabilir, iki müşteri mesajlaşamaz gibi kurallar ekleyebilirsin)
        // if (sender.getRole().equals("CUSTOMER") && receiver.getRole().equals("CUSTOMER")) {
        //     throw new RuntimeException("Müşteriler kendi aralarında mesajlaşamaz.");
        // }

        ChatMessage chatMessage = ChatMessage.builder()
                .senderId(sender.getId())
                .recipientId(receiver.getId())
                .content(messageDto.getContent())
                .timestamp(messageDto.getTimestamp())
                .build();

        ChatMessage saved = messageRepository.save(chatMessage);
        messageDto.setId(saved.getId());
        System.out.println("KAYIT BAŞARILI, ID: " + saved.getId());
        return messageDto;
    }

    @Transactional(readOnly = true)
    public List<ChatMessageDto> getChatHistory(Long user1Id, Long user2Id) {
        if (user1Id.equals(user2Id)) {
            return List.of();
        }
        List<ChatMessage> history = messageRepository.findChatHistory(user1Id, user2Id);
        return messageMapper.toDtoList(history);
    }
}
