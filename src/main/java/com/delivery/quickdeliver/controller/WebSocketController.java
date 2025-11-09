package com.delivery.quickdeliver.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 라이더 위치 업데이트 수신
     * /app/rider/location 으로 메시지를 보내면 /topic/monitoring/riders 로 브로드캐스트
     */
    @MessageMapping("/rider/location")
    @SendTo("/topic/monitoring/riders")
    public Map<String, Object> handleRiderLocation(@Payload Map<String, Object> location) {
        log.debug("Received rider location update: {}", location);
        return location;
    }

    /**
     * 배송 상태 업데이트 수신
     * /app/delivery/status 으로 메시지를 보내면 /topic/monitoring/deliveries 로 브로드캐스트
     */
    @MessageMapping("/delivery/status")
    @SendTo("/topic/monitoring/deliveries")
    public Map<String, Object> handleDeliveryStatus(@Payload Map<String, Object> status) {
        log.debug("Received delivery status update: {}", status);
        return status;
    }

    /**
     * 라이더 연결 등록
     */
    @MessageMapping("/rider/connect")
    public void handleRiderConnect(@Payload Map<String, Object> message,
                                   SimpMessageHeaderAccessor headerAccessor) {
        String riderId = (String) message.get("riderId");
        log.info("Rider {} connected to WebSocket", riderId);
        
        // 세션에 riderId 저장 (Null 안전)
        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
        if (sessionAttributes != null) {
            sessionAttributes.put("riderId", riderId);
            log.debug("Stored riderId in session: {}", riderId);
        } else {
            log.warn("Session attributes is null, cannot store riderId");
        }
        
        // 연결 성공 메시지 전송
        if (riderId != null) {
            messagingTemplate.convertAndSendToUser(
                    riderId,
                    "/queue/connection",
                    Map.of("status", "connected", "message", "WebSocket connection established")
            );
        }
    }

    /**
     * 긴급 알림 전송 (관리자 -> 특정 라이더)
     */
    @MessageMapping("/admin/urgent")
    public void sendUrgentMessage(@Payload Map<String, Object> message) {
        String riderId = (String) message.get("riderId");
        String urgentMessage = (String) message.get("message");
        
        if (riderId == null || urgentMessage == null) {
            log.warn("Invalid urgent message: riderId or message is null");
            return;
        }
        
        log.info("Sending urgent message to rider {}: {}", riderId, urgentMessage);
        
        messagingTemplate.convertAndSendToUser(
                riderId,
                "/queue/urgent",
                Map.of("type", "URGENT", "message", urgentMessage, "timestamp", System.currentTimeMillis())
        );
    }

    /**
     * 채팅 메시지 (고객 지원)
     */
    @MessageMapping("/chat/send")
    public void handleChatMessage(@Payload Map<String, Object> chatMessage) {
        String sender = (String) chatMessage.get("sender");
        String recipient = (String) chatMessage.get("recipient");
        String message = (String) chatMessage.get("message");
        
        if (sender == null || recipient == null || message == null) {
            log.warn("Invalid chat message: sender, recipient or message is null");
            return;
        }
        
        log.debug("Chat message from {} to {}: {}", sender, recipient, message);
        
        // 수신자에게 메시지 전송
        messagingTemplate.convertAndSendToUser(
                recipient,
                "/queue/chat",
                Map.of(
                        "sender", sender,
                        "message", message,
                        "timestamp", System.currentTimeMillis()
                )
        );
    }
}
