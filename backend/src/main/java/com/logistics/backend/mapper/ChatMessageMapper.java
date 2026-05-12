package com.logistics.backend.mapper;

import com.logistics.backend.entity.business.ChatMessage;
import com.logistics.backend.payload.chat.ChatMessageDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ChatMessageMapper {
    // Entity -> DTO Çevirisi
    public ChatMessageDto toDto(ChatMessage entity) {
        if (entity == null) return null;

        return ChatMessageDto.builder()
                .id(entity.getId())
                .senderId(entity.getSenderId())
                .recipientId(entity.getRecipientId())
                .content(entity.getContent())
                .timestamp(entity.getTimestamp())
                .build();
    }

    // Liste Çevirisi (Geçmiş mesajları çekerken çok işine yarayacak)
    public List<ChatMessageDto> toDtoList(List<ChatMessage> entities) {
        if (entities == null) return null;

        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
