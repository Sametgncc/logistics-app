package com.logistics.backend.security.config;

import com.logistics.backend.entity.user.User;
import com.logistics.backend.repository.user.UserRepository;
import com.logistics.backend.security.jwt.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Principal;

/** Authenticates STOMP CONNECT frames using the JWT in the native Authorization header. */
@Component
@RequiredArgsConstructor
public class WebSocketAuthenticationInterceptor implements ChannelInterceptor {

    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = bearerToken(accessor.getFirstNativeHeader("Authorization"));
            if (token == null || !jwtUtils.validateJwtToken(token)) {
                throw new AccessDeniedException("Geçerli bir JWT ile bağlanmalısınız");
            }

            Long userId = jwtUtils.getUserIdFromJwtToken(token);
            User user = userRepository.findById(userId)
                    .filter(userEntity -> Boolean.TRUE.equals(userEntity.getEnabled()))
                    .orElseThrow(() -> new AccessDeniedException("Aktif kullanıcı bulunamadı"));

            Principal principal = () -> String.valueOf(user.getId());
            accessor.setUser(principal);
        }

        if ((StompCommand.SEND.equals(accessor.getCommand()) || StompCommand.SUBSCRIBE.equals(accessor.getCommand()))
                && accessor.getUser() == null) {
            throw new AccessDeniedException("WebSocket bağlantısı doğrulanmadı");
        }
        return message;
    }

    private String bearerToken(String authorization) {
        if (!StringUtils.hasText(authorization) || !authorization.startsWith("Bearer ")) {
            return null;
        }
        return authorization.substring(7);
    }
}
