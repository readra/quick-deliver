package com.delivery.quickdeliver.domain.enums;

import lombok.Getter;

@Getter
public enum UserRole {
    ADMIN("관리자"),
    BACKOFFICE("백오피스"),
    RIDER("라이더"),
    CUSTOMER("고객");

    private final String description;

    UserRole(String description) {
        this.description = description;
    }

}
