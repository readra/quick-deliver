package com.delivery.quickdeliver.domain.enums;

import lombok.Getter;

@Getter
public enum VehicleType {
    BIKE("자전거", 5, 2.0),
    MOTORCYCLE("오토바이", 10, 5.0),
    CAR("자동차", 20, 10.0),
    TRUCK("트럭", 50, 20.0);

    private final String description;
    private final int maxWeight;  // kg
    private final double maxDistance;  // km

    VehicleType(String description, int maxWeight, double maxDistance) {
        this.description = description;
        this.maxWeight = maxWeight;
        this.maxDistance = maxDistance;
    }

}
