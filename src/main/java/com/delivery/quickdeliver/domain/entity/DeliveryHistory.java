package com.delivery.quickdeliver.domain.entity;

import com.delivery.quickdeliver.domain.enums.DeliveryStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_histories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_id", nullable = false)
    private Delivery delivery;

    @Column(nullable = false)
    private String event;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;

    private Double latitude;
    private Double longitude;

    @Column(nullable = false)
    private LocalDateTime eventTime;

    @PrePersist
    public void prePersist() {
        this.eventTime = LocalDateTime.now();
    }
}
