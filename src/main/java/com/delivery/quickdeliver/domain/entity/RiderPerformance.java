package com.delivery.quickdeliver.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "rider_performances")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiderPerformance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rider_id", nullable = false)
    private Rider rider;

    @Column(nullable = false)
    private LocalDate date;

    // 일일 성과 지표
    private Integer totalDeliveries;
    private Double totalDistance; // km
    private Integer totalWorkMinutes;
    private Double averageDeliveryTime; // minutes
    private Double averageRating;
    private Integer onTimeDeliveries;
    private Integer lateDeliveries;

    // 효율성 지표
    private Double deliveryEfficiencyScore; // 0-100
    private Double routeOptimizationScore; // 0-100

    // 수익
    private Integer totalEarnings;
    private Integer bonusEarnings;

    // 이슈 및 피드백
    private Integer customerComplaints;
    private Integer merchantComplaints;

    public void calculateEfficiencyScore() {
        // 배송 효율성 점수 계산 로직
        double onTimeRate = totalDeliveries > 0 ? 
            (double) onTimeDeliveries / totalDeliveries * 100 : 0;
        double avgTimeScore = averageDeliveryTime < 30 ? 100 : 
            Math.max(0, 100 - (averageDeliveryTime - 30) * 2);
        
        this.deliveryEfficiencyScore = (onTimeRate + avgTimeScore) / 2;
    }
}
