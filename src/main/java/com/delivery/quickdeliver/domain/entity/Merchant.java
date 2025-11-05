package com.delivery.quickdeliver.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "merchants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Merchant extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String merchantId;

    @Column(nullable = false)
    private String name;

    private String businessNumber;

    @Embedded
    private Address address;

    @Column(nullable = false)
    private String ownerName;

    @Column(nullable = false)
    private String contactPhone;

    private String email;

    private Boolean isActive;

    // 운영 시간
    private String operatingHours; // JSON 형태로 저장

    @OneToMany(mappedBy = "merchant", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Delivery> deliveries = new ArrayList<>();

    // 통계
    @Column(columnDefinition = "int default 0")
    private Integer totalOrders;

    @Column(columnDefinition = "double default 5.0")
    private Double averageRating;
}
