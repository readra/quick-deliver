package com.delivery.quickdeliver.dto.route;

import lombok.Data;

@Data
public class Waypoint {
    private Double latitude;
    private Double longitude;
    private Integer timestamp; // 초 단위
    private String description;
}
