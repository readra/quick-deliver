package com.delivery.quickdeliver.dto.request;

import com.delivery.quickdeliver.domain.entity.Address;
import com.delivery.quickdeliver.domain.enums.Priority;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryCreateRequest {

    @NotNull(message = "주문번호는 필수입니다")
    private String orderNumber;

    @NotNull(message = "픽업 주소는 필수입니다")
    private Address pickupAddress;

    @NotNull(message = "배송 주소는 필수입니다")
    private Address deliveryAddress;

    @NotNull(message = "우선순위는 필수입니다")
    private Priority priority;

    private String itemDescription;

    @Positive(message = "무게는 양수여야 합니다")
    private Double weight;

    @Positive(message = "수량은 양수여야 합니다")
    private Integer quantity;

    @Positive(message = "배송료는 양수여야 합니다")
    private Integer deliveryFee;

    private String specialInstructions;

    private Long merchantId;
}
