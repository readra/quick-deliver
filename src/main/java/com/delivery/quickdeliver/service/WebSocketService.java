package com.delivery.quickdeliver.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendToRider(String riderId, String eventType, String message) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", eventType);
        payload.put("message", message);
        payload.put("timestamp", System.currentTimeMillis());

        messagingTemplate.convertAndSend("/topic/rider/" + riderId, payload);
        log.debug("Sent WebSocket message to rider {}: {}", riderId, eventType);
    }

    public void sendToCustomer(String orderNumber, String eventType, String message) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", eventType);
        payload.put("message", message);
        payload.put("timestamp", System.currentTimeMillis());

        messagingTemplate.convertAndSend("/topic/order/" + orderNumber, payload);
        log.debug("Sent WebSocket message for order {}: {}", orderNumber, eventType);
    }

    public void broadcastRiderLocation(String riderId, Double latitude, Double longitude) {
        Map<String, Object> location = new HashMap<>();
        location.put("riderId", riderId);
        location.put("latitude", latitude);
        location.put("longitude", longitude);
        location.put("timestamp", System.currentTimeMillis());

        // 관제센터로 브로드캐스트
        messagingTemplate.convertAndSend("/topic/monitoring/riders", location);
        log.debug("Broadcast rider {} location: {}, {}", riderId, latitude, longitude);
    }

    public void broadcastDeliveryUpdate(String deliveryId, String status) {
        Map<String, Object> update = new HashMap<>();
        update.put("deliveryId", deliveryId);
        update.put("status", status);
        update.put("timestamp", System.currentTimeMillis());

        // 관제센터로 브로드캐스트
        messagingTemplate.convertAndSend("/topic/monitoring/deliveries", update);
        log.debug("Broadcast delivery {} status: {}", deliveryId, status);
    }
}
