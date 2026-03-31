package com.delivery.quickdeliver.domain.enums;

import lombok.Getter;

@Getter
public enum Priority {
    LOW("일반", 120),
    NORMAL("보통", 60),
    HIGH("긴급", 30),
    URGENT("특급", 15);

    private final String description;
    private final int maxMinutes;

    Priority(String description, int maxMinutes) {
        this.description = description;
        this.maxMinutes = maxMinutes;
    }

}
