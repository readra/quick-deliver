package com.delivery.quickdeliver.domain.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {
    private String address;
    private String detailAddress;
    private Double latitude;
    private Double longitude;
    private String contactName;
    private String contactPhone;
}
