package com.delivery.quickdeliver.dto.response;

import com.delivery.quickdeliver.domain.entity.Address;
import com.delivery.quickdeliver.domain.entity.Delivery;
import com.delivery.quickdeliver.domain.enums.DeliveryStatus;
import com.delivery.quickdeliver.domain.enums.Priority;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryResponse {

    private Long id;
    private String deliveryId;
    private String orderNumber;
    private Address pickupAddress;
    private Address deliveryAddress;
    private DeliveryStatus status;
    private Priority priority;
    private String itemDescription;
    private Double weight;
    private Integer quantity;
    private Integer deliveryFee;
    private LocalDateTime requestedTime;
    private LocalDateTime estimatedDeliveryTime;
    private LocalDateTime actualDeliveryTime;
    private Double estimatedDistance;
    private String riderId;
    private String riderName;
    private Integer rating;
    private String feedback;

    public static DeliveryResponse from(Delivery delivery) {
        return DeliveryResponse.builder()
                .id(delivery.getId())
                .deliveryId(delivery.getDeliveryId())
                .orderNumber(delivery.getOrderNumber())
                .pickupAddress(delivery.getPickupAddress())
                .deliveryAddress(delivery.getDeliveryAddress())
                .status(delivery.getStatus())
                .priority(delivery.getPriority())
                .itemDescription(delivery.getItemDescription())
                .weight(delivery.getWeight())
                .quantity(delivery.getQuantity())
                .deliveryFee(delivery.getDeliveryFee())
                .requestedTime(delivery.getRequestedTime())
                .estimatedDeliveryTime(delivery.getEstimatedDeliveryTime())
                .actualDeliveryTime(delivery.getActualDeliveryTime())
                .estimatedDistance(delivery.getEstimatedDistance())
                .riderId(delivery.getRider() != null ? delivery.getRider().getRiderId() : null)
                .riderName(delivery.getRider() != null ? delivery.getRider().getName() : null)
                .rating(delivery.getRating())
                .feedback(delivery.getFeedback())
                .build();
    }
}
