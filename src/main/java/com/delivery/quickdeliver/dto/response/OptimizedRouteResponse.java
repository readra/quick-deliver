package com.delivery.quickdeliver.dto.response;

import com.delivery.quickdeliver.domain.entity.Address;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OptimizedRouteResponse {
    private Double totalDistance;
    private Integer estimatedTime;
    private List<Waypoint> waypoints;
    private Double optimizationScore;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Waypoint {
        private String deliveryId;
        private String type; // PICKUP or DELIVERY
        private Address address;
        private Integer estimatedArrival;
    }
}
