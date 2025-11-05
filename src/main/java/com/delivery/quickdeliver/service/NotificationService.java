package com.delivery.quickdeliver.service;

import com.delivery.quickdeliver.domain.entity.Delivery;
import com.delivery.quickdeliver.domain.entity.Rider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final WebSocketService webSocketService;

    public void notifyRiderAssignment(Rider rider, Delivery delivery) {
        String message = String.format(
                "새로운 배송이 할당되었습니다! 주문번호: %s, 픽업: %s",
                delivery.getOrderNumber(),
                delivery.getPickupAddress().getAddress()
        );

        // WebSocket으로 실시간 알림
        webSocketService.sendToRider(rider.getRiderId(), "DELIVERY_ASSIGNED", message);
        
        // TODO: FCM Push 알림 추가
        log.info("Notified rider {} about new delivery {}", 
                rider.getRiderId(), delivery.getDeliveryId());
    }

    public void notifyStatusChange(Delivery delivery) {
        String message = String.format(
                "배송 상태 업데이트: %s - %s",
                delivery.getOrderNumber(),
                delivery.getStatus().getDescription()
        );

        // 고객에게 알림
        webSocketService.sendToCustomer(delivery.getOrderNumber(), "STATUS_UPDATE", message);
        
        log.info("Notified customer about delivery {} status change to {}", 
                delivery.getDeliveryId(), delivery.getStatus());
    }

    public void notifyDeliveryDelay(Delivery delivery, int delayMinutes) {
        String message = String.format(
                "배송이 약 %d분 지연될 예정입니다. 불편을 드려 죄송합니다.",
                delayMinutes
        );

        webSocketService.sendToCustomer(delivery.getOrderNumber(), "DELIVERY_DELAY", message);
        
        log.info("Notified about {} minutes delay for delivery {}", 
                delayMinutes, delivery.getDeliveryId());
    }

    public void notifyUrgentDelivery(Rider rider, Delivery delivery) {
        String message = String.format(
                "긴급 배송! 주문번호: %s, %d분 이내 완료 필요",
                delivery.getOrderNumber(),
                delivery.getPriority().getMaxMinutes()
        );

        webSocketService.sendToRider(rider.getRiderId(), "URGENT_DELIVERY", message);
        
        log.info("Sent urgent delivery notification to rider {}", rider.getRiderId());
    }
}
