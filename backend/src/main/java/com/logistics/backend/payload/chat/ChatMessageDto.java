package com.logistics.backend.payload.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDto {

    private Long id;
    private Long senderId;
    private Long recipientId;
    private String content;
    private LocalDateTime timestamp;

}