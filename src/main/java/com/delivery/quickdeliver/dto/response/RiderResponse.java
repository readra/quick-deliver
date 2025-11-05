package com.delivery.quickdeliver.dto.response;

import com.delivery.quickdeliver.domain.entity.Rider;
import com.delivery.quickdeliver.domain.enums.RiderStatus;
import com.delivery.quickdeliver.domain.enums.VehicleType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiderResponse {

    private Long id;
    private String riderId;
    private String name;
    private String phoneNumber;
    private String email;
    private RiderStatus status;
    private VehicleType vehicleType;
    private String vehicleNumber;
    private Double currentLatitude;
    private Double currentLongitude;
    private LocalDateTime lastLocationUpdate;
    private Integer totalDeliveries;
    private Double averageRating;
    private LocalDateTime shiftStartTime;

    public static RiderResponse from(Rider rider) {
        return RiderResponse.builder()
                .id(rider.getId())
                .riderId(rider.getRiderId())
                .name(rider.getName())
                .phoneNumber(rider.getPhoneNumber())
                .email(rider.getEmail())
                .status(rider.getStatus())
                .vehicleType(rider.getVehicleType())
                .vehicleNumber(rider.getVehicleNumber())
                .currentLatitude(rider.getCurrentLatitude())
                .currentLongitude(rider.getCurrentLongitude())
                .lastLocationUpdate(rider.getLastLocationUpdate())
                .totalDeliveries(rider.getTotalDeliveries())
                .averageRating(rider.getAverageRating())
                .shiftStartTime(rider.getShiftStartTime())
                .build();
    }
}
