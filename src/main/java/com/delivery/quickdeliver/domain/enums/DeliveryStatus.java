package com.delivery.quickdeliver.domain.enums;

public enum DeliveryStatus {
    PENDING("대기 중"),
    ASSIGNED("배송원 할당됨"),
    PICKING_UP("픽업 중"),
    IN_TRANSIT("배송 중"),
    DELIVERED("배송 완료"),
    CANCELLED("취소됨"),
    FAILED("배송 실패");

    private final String description;

    DeliveryStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
