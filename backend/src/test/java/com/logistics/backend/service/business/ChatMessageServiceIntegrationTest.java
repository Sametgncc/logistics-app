package com.logistics.backend.service.business;

import com.logistics.backend.entity.business.ChatMessage;
import com.logistics.backend.entity.user.User;
import com.logistics.backend.exception.BadRequestException;
import com.logistics.backend.payload.chat.ChatMessageDto;
import com.logistics.backend.repository.business.ChatMessageRepository;
import com.logistics.backend.repository.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class ChatMessageServiceIntegrationTest {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void cleanUp() {
        chatMessageRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void savesMessageWithAuthenticatedSenderAndServerTimestamp() {
        User sender = userRepository.save(user("sender@example.com"));
        User recipient = userRepository.save(user("recipient@example.com"));
        LocalDateTime clientTimestamp = LocalDateTime.of(2000, 1, 1, 0, 0);

        ChatMessageDto incoming = ChatMessageDto.builder()
                .senderId(recipient.getId())
                .recipientId(recipient.getId())
                .content("Merhaba")
                .timestamp(clientTimestamp)
                .build();

        ChatMessageDto saved = chatMessageService.saveMessage(sender.getId(), incoming);
        List<ChatMessage> persistedMessages = chatMessageRepository.findAll();

        assertEquals(1, persistedMessages.size());
        assertEquals(sender.getId(), saved.getSenderId());
        assertEquals(recipient.getId(), saved.getRecipientId());
        assertEquals("Merhaba", saved.getContent());
        assertNotNull(saved.getTimestamp());
        assertNotEquals(clientTimestamp, saved.getTimestamp());
        assertEquals(List.of(saved), chatMessageService.getChatHistory(sender.getId(), recipient.getId()));
    }

    @Test
    void rejectsMessagesToSelf() {
        User sender = userRepository.save(user("self@example.com"));
        ChatMessageDto incoming = ChatMessageDto.builder()
                .recipientId(sender.getId())
                .content("Kendime")
                .build();

        assertThrows(BadRequestException.class, () -> chatMessageService.saveMessage(sender.getId(), incoming));
        assertTrue(chatMessageRepository.findAll().isEmpty());
    }

    private User user(String email) {
        return User.builder()
                .name("Test")
                .lastName("User")
                .email(email)
                .enabled(true)
                .build();
    }
}
