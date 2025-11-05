package com.delivery.quickdeliver.domain.entity;

import com.delivery.quickdeliver.domain.enums.RiderStatus;
import com.delivery.quickdeliver.domain.enums.VehicleType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "riders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rider extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String riderId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String phoneNumber;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleType vehicleType;

    private String vehicleNumber;

    // 현재 위치 정보
    private Double currentLatitude;
    private Double currentLongitude;
    private LocalDateTime lastLocationUpdate;

    // 성과 지표
    @Column(columnDefinition = "int default 0")
    private Integer totalDeliveries;

    @Column(columnDefinition = "double default 5.0")
    private Double averageRating;

    @Column(columnDefinition = "double default 0")
    private Double totalDistance;

    // 근무 시간 관리
    private LocalDateTime shiftStartTime;
    private LocalDateTime shiftEndTime;

    @OneToMany(mappedBy = "rider", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Delivery> deliveries = new ArrayList<>();

    @OneToMany(mappedBy = "rider", cascade = CascadeType.ALL)
    @Builder.Default
    private List<RiderPerformance> performances = new ArrayList<>();

    // 라이더 상태 업데이트
    public void updateLocation(Double latitude, Double longitude) {
        this.currentLatitude = latitude;
        this.currentLongitude = longitude;
        this.lastLocationUpdate = LocalDateTime.now();
    }

    public void startShift() {
        this.shiftStartTime = LocalDateTime.now();
        this.status = RiderStatus.AVAILABLE;
    }

    public void endShift() {
        this.shiftEndTime = LocalDateTime.now();
        this.status = RiderStatus.OFFLINE;
    }
}
