package com.delivery.quickdeliver.domain.enums;

public enum Priority {
    LOW("일반", 120),      // 2시간 내
    NORMAL("보통", 60),    // 1시간 내
    HIGH("긴급", 30),      // 30분 내
    URGENT("특급", 15);    // 15분 내

    private final String description;
    private final int maxMinutes;

    Priority(String description, int maxMinutes) {
        this.description = description;
        this.maxMinutes = maxMinutes;
    }

    public String getDescription() {
        return description;
    }

    public int getMaxMinutes() {
        return maxMinutes;
    }
}
