package com.delivery.quickdeliver.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        
        log.info("New WebSocket connection established. Session ID: {}", sessionId);
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        // Optional을 사용한 Null 안전 처리
        Optional.ofNullable(headerAccessor.getSessionAttributes())
                .map(attrs -> attrs.get("username"))
                .map(String.class::cast)
                .ifPresentOrElse(
                    username -> {
                        log.info("User {} disconnected. Session ID: {}", username, sessionId);
                        messagingTemplate.convertAndSend("/topic/monitoring/disconnect", 
                                "User " + username + " disconnected");
                    },
                    () -> log.info("Anonymous user disconnected. Session ID: {}", sessionId)
                );
    }
}
