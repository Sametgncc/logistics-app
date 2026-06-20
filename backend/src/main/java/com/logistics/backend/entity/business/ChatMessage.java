package com.logistics.backend.entity.business;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(indexes = {
        @Index(name = "idx_chat_sender_recipient_timestamp", columnList = "senderId,recipientId,timestamp"),
        @Index(name = "idx_chat_recipient_sender_timestamp", columnList = "recipientId,senderId,timestamp")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long senderId;

    @Column(nullable = false)
    private Long recipientId;

    @Column(nullable = false, length = 4000)
    private String content;

    @Column(nullable = false)
    private LocalDateTime timestamp;
}
