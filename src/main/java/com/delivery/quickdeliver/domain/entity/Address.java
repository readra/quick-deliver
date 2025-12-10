package com.delivery.quickdeliver.domain.entity;

import jakarta.persistence.Column;
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
    
    @Column(name = "address_contact_name")
    private String contactName;
    
    @Column(name = "address_contact_phone")
    private String contactPhone;
}
