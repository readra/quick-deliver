package com.delivery.quickdeliver.controller;

import com.delivery.quickdeliver.dto.request.DeliveryCreateRequest;
import com.delivery.quickdeliver.dto.request.DeliveryStatusUpdateRequest;
import com.delivery.quickdeliver.dto.response.ApiResponse;
import com.delivery.quickdeliver.dto.response.DeliveryResponse;
import com.delivery.quickdeliver.dto.response.DeliveryTrackingResponse;
import com.delivery.quickdeliver.service.DeliveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
@Tag(name = "Delivery API", description = "배송 관리 API")
public class DeliveryController {

    private final DeliveryService deliveryService;

    @PostMapping
    @Operation(summary = "배송 생성", 
               description = "새로운 배송을 생성하고 자동으로 최적의 라이더를 배정합니다.")
    public ResponseEntity<ApiResponse<DeliveryResponse>> createDelivery(
            @Valid @RequestBody DeliveryCreateRequest request) {
        log.info("Creating new delivery for order: {}", request.getOrderNumber());
        
        DeliveryResponse response = deliveryService.createDelivery(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("배송이 생성되었습니다.", response));
    }

    @GetMapping("/{deliveryId}")
    @Operation(summary = "배송 상세 조회", description = "배송 ID로 배송 상세 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<DeliveryResponse>> getDelivery(
            @Parameter(description = "배송 ID") @PathVariable String deliveryId) {
        log.info("Getting delivery info: {}", deliveryId);
        
        // DeliveryService에 getDelivery 메소드 추가 필요
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @PutMapping("/{deliveryId}/status")
    @Operation(summary = "배송 상태 업데이트", 
               description = "배송 상태를 업데이트합니다. 상태 변경 시 고객에게 자동으로 알림이 전송됩니다.")
    public ResponseEntity<ApiResponse<DeliveryResponse>> updateDeliveryStatus(
            @Parameter(description = "배송 ID") @PathVariable String deliveryId,
            @Valid @RequestBody DeliveryStatusUpdateRequest request) {
        log.info("Updating delivery {} status to {}", deliveryId, request.getStatus());
        
        DeliveryResponse response = deliveryService.updateDeliveryStatus(deliveryId, request);
        
        return ResponseEntity.ok(ApiResponse.success("배송 상태가 업데이트되었습니다.", response));
    }

    @GetMapping("/{deliveryId}/track")
    @Operation(summary = "배송 추적", 
               description = "실시간 배송 추적 정보를 조회합니다. 현재 위치, 상태, 이력을 포함합니다.")
    public ResponseEntity<ApiResponse<DeliveryTrackingResponse>> trackDelivery(
            @Parameter(description = "배송 ID") @PathVariable String deliveryId) {
        log.info("Tracking delivery: {}", deliveryId);
        
        DeliveryTrackingResponse tracking = deliveryService.trackDelivery(deliveryId);
        
        return ResponseEntity.ok(ApiResponse.success(tracking));
    }

    @GetMapping("/pending")
    @Operation(summary = "대기 중인 배송 목록", 
               description = "아직 라이더에게 배정되지 않은 대기 중인 배송 목록을 우선순위 순으로 조회합니다.")
    public ResponseEntity<ApiResponse<List<DeliveryResponse>>> getPendingDeliveries() {
        log.info("Getting pending deliveries");
        
        List<DeliveryResponse> deliveries = deliveryService.getPendingDeliveries();
        
        return ResponseEntity.ok(ApiResponse.success(deliveries));
    }

    @PostMapping("/{deliveryId}/assign/{riderId}")
    @Operation(summary = "라이더 수동 배정", 
               description = "특정 배송을 특정 라이더에게 수동으로 배정합니다.")
    public ResponseEntity<ApiResponse<DeliveryResponse>> assignRider(
            @Parameter(description = "배송 ID") @PathVariable String deliveryId,
            @Parameter(description = "라이더 ID") @PathVariable String riderId) {
        log.info("Manually assigning delivery {} to rider {}", deliveryId, riderId);
        
        // DeliveryService에 manualAssign 메소드 추가 필요
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @PostMapping("/{deliveryId}/cancel")
    @Operation(summary = "배송 취소", description = "배송을 취소합니다.")
    public ResponseEntity<ApiResponse<Void>> cancelDelivery(
            @Parameter(description = "배송 ID") @PathVariable String deliveryId,
            @Parameter(description = "취소 사유") @RequestParam String reason) {
        log.info("Canceling delivery {}: {}", deliveryId, reason);
        
        DeliveryStatusUpdateRequest request = new DeliveryStatusUpdateRequest();
        request.setStatus(com.delivery.quickdeliver.domain.enums.DeliveryStatus.CANCELLED);
        request.setReason(reason);
        
        deliveryService.updateDeliveryStatus(deliveryId, request);
        
        return ResponseEntity.ok(ApiResponse.success("배송이 취소되었습니다."));
    }

    @PostMapping("/{deliveryId}/rating")
    @Operation(summary = "배송 평가", description = "완료된 배송에 대한 평점을 등록합니다.")
    public ResponseEntity<ApiResponse<Void>> rateDelivery(
            @Parameter(description = "배송 ID") @PathVariable String deliveryId,
            @Parameter(description = "평점 (1-5)") @RequestParam Integer rating) {
        log.info("Rating delivery {}: {} stars", deliveryId, rating);
        
        // TODO: DeliveryService에 rateDelivery 메소드 추가 필요
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @GetMapping("/search")
    @Operation(summary = "배송 검색", 
               description = "주문번호 또는 배송ID로 배송을 검색합니다.")
    public ResponseEntity<ApiResponse<DeliveryResponse>> searchDelivery(
            @Parameter(description = "주문번호 또는 배송ID") @RequestParam String query) {
        log.info("Searching delivery with query: {}", query);
        
        // TODO: DeliveryService에 search 메소드 추가 필요
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
