package com.delivery.quickdeliver.dto.response;

import com.delivery.quickdeliver.domain.enums.RiderStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiderDashboardResponse {

    private String riderId;
    private String name;
    private RiderStatus status;
    private Integer todayDeliveries;
    private Integer totalDeliveries;
    private Double averageRating;
    private Double averageDeliveryTime;
    private Location currentLocation;
    private LocalDateTime shiftStartTime;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Location {
        private Double latitude;
        private Double longitude;
    }
}
