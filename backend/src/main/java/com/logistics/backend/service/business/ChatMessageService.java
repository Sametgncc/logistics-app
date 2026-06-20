package com.logistics.backend.service.business;

import com.logistics.backend.entity.business.ChatMessage;
import com.logistics.backend.entity.user.User;
import com.logistics.backend.exception.BadRequestException;
import com.logistics.backend.exception.ResourceNotFoundException;
import com.logistics.backend.mapper.ChatMessageMapper;
import com.logistics.backend.payload.chat.ChatMessageDto;
import com.logistics.backend.repository.business.ChatMessageRepository;
import com.logistics.backend.repository.user.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;


@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChatMessageMapper messageMapper;

    @Transactional
    public ChatMessageDto saveMessage(Long authenticatedSenderId, ChatMessageDto messageDto) {
        if (authenticatedSenderId.equals(messageDto.getRecipientId())) {
            throw new BadRequestException("Kendinize mesaj gönderemezsiniz");
        }

        User sender = userRepository.findById(authenticatedSenderId)
                .orElseThrow(() -> new ResourceNotFoundException("Gönderen kullanıcı bulunamadı"));

        User receiver = userRepository.findById(messageDto.getRecipientId())
                .orElseThrow(() -> new ResourceNotFoundException("Alıcı kullanıcı bulunamadı"));

        ChatMessage chatMessage = ChatMessage.builder()
                .senderId(sender.getId())
                .recipientId(receiver.getId())
                .content(messageDto.getContent())
                .timestamp(LocalDateTime.now(ZoneOffset.UTC))
                .build();

        ChatMessage saved = messageRepository.save(chatMessage);
        return messageMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<ChatMessageDto> getChatHistory(Long authenticatedUserId, Long otherUserId) {
        if (authenticatedUserId.equals(otherUserId)) {
            return List.of();
        }

        if (!userRepository.existsById(otherUserId)) {
            throw new ResourceNotFoundException("Alıcı kullanıcı bulunamadı");
        }

        List<ChatMessage> history = messageRepository.findChatHistory(authenticatedUserId, otherUserId);
        return messageMapper.toDtoList(history);
    }
}
