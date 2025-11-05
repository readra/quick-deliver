package com.delivery.quickdeliver.domain.entity;

import com.delivery.quickdeliver.domain.enums.DeliveryStatus;
import com.delivery.quickdeliver.domain.enums.Priority;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "deliveries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Delivery extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String deliveryId;

    @Column(nullable = false, unique = true)
    private String orderNumber;

    // 배송 정보
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "address", column = @Column(name = "pickup_address")),
            @AttributeOverride(name = "detailAddress", column = @Column(name = "pickup_detail_address")),
            @AttributeOverride(name = "latitude", column = @Column(name = "pickup_latitude")),
            @AttributeOverride(name = "longitude", column = @Column(name = "pickup_longitude")),
            @AttributeOverride(name = "contactName", column = @Column(name = "pickup_contact_name")),
            @AttributeOverride(name = "contactPhone", column = @Column(name = "pickup_contact_phone"))
    })
    private Address pickupAddress;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "address", column = @Column(name = "delivery_address")),
            @AttributeOverride(name = "detailAddress", column = @Column(name = "delivery_detail_address")),
            @AttributeOverride(name = "latitude", column = @Column(name = "delivery_latitude")),
            @AttributeOverride(name = "longitude", column = @Column(name = "delivery_longitude")),
            @AttributeOverride(name = "contactName", column = @Column(name = "delivery_contact_name")),
            @AttributeOverride(name = "contactPhone", column = @Column(name = "delivery_contact_phone"))
    })
    private Address deliveryAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;

    // 배송 물품 정보
    private String itemDescription;
    private Double weight; // kg
    private Integer quantity;
    private Integer deliveryFee;

    // 시간 정보
    private LocalDateTime requestedTime;
    private LocalDateTime estimatedPickupTime;
    private LocalDateTime estimatedDeliveryTime;
    private LocalDateTime actualPickupTime;
    private LocalDateTime actualDeliveryTime;

    // 거리 및 경로 정보
    private Double estimatedDistance; // km
    private Double actualDistance; // km
    
    @Column(columnDefinition = "TEXT")
    private String optimizedRoute; // JSON 형태로 저장될 경로 정보

    // 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rider_id")
    private Rider rider;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id")
    private Merchant merchant;

    @OneToMany(mappedBy = "delivery", cascade = CascadeType.ALL)
    @Builder.Default
    private List<DeliveryHistory> histories = new ArrayList<>();

    // 평가
    private Integer rating;
    private String feedback;

    // 특별 요청사항
    @Column(columnDefinition = "TEXT")
    private String specialInstructions;

    // 배송 상태 변경 메소드
    public void assignRider(Rider rider) {
        this.rider = rider;
        this.status = DeliveryStatus.ASSIGNED;
    }

    public void startPickup() {
        this.status = DeliveryStatus.PICKING_UP;
        this.actualPickupTime = LocalDateTime.now();
    }

    public void completePickup() {
        this.status = DeliveryStatus.IN_TRANSIT;
    }

    public void completeDelivery() {
        this.status = DeliveryStatus.DELIVERED;
        this.actualDeliveryTime = LocalDateTime.now();
    }

    public void cancelDelivery(String reason) {
        this.status = DeliveryStatus.CANCELLED;
        addHistory("배송 취소: " + reason);
    }

    public void addHistory(String event) {
        DeliveryHistory history = DeliveryHistory.builder()
                .delivery(this)
                .event(event)
                .status(this.status)
                .build();
        histories.add(history);
    }
}
