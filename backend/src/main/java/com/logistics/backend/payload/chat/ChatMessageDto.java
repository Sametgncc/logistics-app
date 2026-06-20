package com.logistics.backend.payload.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long senderId;

    @NotNull(message = "Alıcı zorunludur")
    private Long recipientId;

    @NotBlank(message = "Mesaj boş olamaz")
    @Size(max = 4000, message = "Mesaj en fazla 4000 karakter olabilir")
    private String content;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime timestamp;

}
