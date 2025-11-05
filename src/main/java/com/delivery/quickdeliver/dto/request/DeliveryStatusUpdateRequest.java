package com.delivery.quickdeliver.dto.request;

import com.delivery.quickdeliver.domain.enums.DeliveryStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryStatusUpdateRequest {

    @NotNull(message = "배송 상태는 필수입니다")
    private DeliveryStatus status;

    private Double latitude;
    private Double longitude;
    private String reason; // 취소 사유 등
}
