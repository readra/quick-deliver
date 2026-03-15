package com.delivery.quickdeliver.controller;

import com.delivery.quickdeliver.domain.entity.Rider;
import com.delivery.quickdeliver.domain.enums.RiderStatus;
import com.delivery.quickdeliver.repository.RiderRepository;
import com.delivery.quickdeliver.service.GeofencingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final RiderRepository riderRepository;
    private final GeofencingService geofencingService;

    /**
     * 라이더 위치 업데이트 수신 → DB 저장 + 관제 브로드캐스트
     * 라이더 앱: /app/rider/location 으로 전송
     * 관제 센터: /topic/monitoring/riders 로 수신
     */
    @MessageMapping("/rider/location")
    @Transactional
    public void handleRiderLocation(@Payload Map<String, Object> payload) {
        String riderId = (String) payload.get("riderId");
        if (riderId == null) {
            log.warn("위치 업데이트 수신: riderId 없음");
            return;
        }

        Double latitude  = toDouble(payload.get("latitude"));
        Double longitude = toDouble(payload.get("longitude"));
        if (latitude == null || longitude == null) {
            log.warn("위치 업데이트 수신: 좌표 없음 riderId={}", riderId);
            return;
        }

        // DB 저장
        Rider rider = riderRepository.findByRiderId(riderId).orElse(null);
        if (rider == null) {
            log.warn("위치 업데이트 수신: 존재하지 않는 라이더 riderId={}", riderId);
            return;
        }

        rider.setCurrentLatitude(latitude);
        rider.setCurrentLongitude(longitude);
        rider.setLastLocationUpdate(LocalDateTime.now());
        riderRepository.save(rider);

        // 관제 지도에 필요한 데이터를 enriched 형태로 브로드캐스트
        Map<String, Object> broadcast = new HashMap<>();
        broadcast.put("riderId", riderId);
        broadcast.put("name", rider.getName());
        broadcast.put("status", rider.getStatus().name());
        broadcast.put("vehicleType", rider.getVehicleType() != null ? rider.getVehicleType().name() : null);
        broadcast.put("latitude", latitude);
        broadcast.put("longitude", longitude);
        broadcast.put("timestamp", System.currentTimeMillis());

        messagingTemplate.convertAndSend("/topic/monitoring/riders", broadcast);

        log.debug("위치 업데이트 처리 완료: {} ({}, {})", riderId, latitude, longitude);

        // 지오펜싱 체크: BUSY 라이더만 자동 상태 전환 대상
        if (rider.getStatus() == RiderStatus.BUSY) {
            geofencingService.checkAndAutoTransition(riderId, latitude, longitude);
        }
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
     * Double 변환 헬퍼 (WebSocket payload는 Number 또는 String으로 올 수 있음)
     */
    private Double toDouble(Object value) {
        if (value instanceof Number) return ((Number) value).doubleValue();
        if (value instanceof String) {
            try { return Double.parseDouble((String) value); } catch (NumberFormatException ignored) {}
        }
        return null;
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
