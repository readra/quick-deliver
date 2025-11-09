package com.delivery.quickdeliver.domain.enums;

import lombok.Getter;

@Getter
public enum RiderStatus {
    OFFLINE("오프라인"),
    AVAILABLE("배송 가능"),
    BUSY("배송 중"),
    BREAK("휴식 중"),
    RETURNING("복귀 중");

    private final String description;

    RiderStatus(String description) {
        this.description = description;
    }

}
