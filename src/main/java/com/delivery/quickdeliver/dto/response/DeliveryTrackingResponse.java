package com.delivery.quickdeliver.dto.response;

import com.delivery.quickdeliver.domain.enums.DeliveryStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryTrackingResponse {

    private String deliveryId;
    private DeliveryStatus currentStatus;
    private Location currentLocation;
    private LocalDateTime estimatedDeliveryTime;
    private List<History> histories;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Location {
        private Double latitude;
        private Double longitude;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class History {
        private String event;
        private DeliveryStatus status;
        private LocalDateTime eventTime;
        private Location location;
    }
}
