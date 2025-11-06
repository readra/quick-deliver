package com.delivery.quickdeliver.controller;

import com.delivery.quickdeliver.domain.enums.RiderStatus;
import com.delivery.quickdeliver.dto.request.LocationUpdateRequest;
import com.delivery.quickdeliver.dto.request.RiderRegisterRequest;
import com.delivery.quickdeliver.dto.response.ApiResponse;
import com.delivery.quickdeliver.dto.response.DeliveryResponse;
import com.delivery.quickdeliver.dto.response.RiderDashboardResponse;
import com.delivery.quickdeliver.dto.response.RiderResponse;
import com.delivery.quickdeliver.service.DeliveryService;
import com.delivery.quickdeliver.service.RiderService;
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
@RequestMapping("/api/riders")
@RequiredArgsConstructor
@Tag(name = "Rider API", description = "라이더 모바일 앱 API")
public class RiderController {

    private final RiderService riderService;
    private final DeliveryService deliveryService;

    @PostMapping("/register")
    @Operation(summary = "라이더 등록", description = "새로운 라이더를 등록합니다.")
    public ResponseEntity<ApiResponse<RiderResponse>> registerRider(
            @Valid @RequestBody RiderRegisterRequest request) {
        log.info("Registering new rider: {}", request.getEmail());
        
        RiderResponse response = riderService.registerRider(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("라이더 등록이 완료되었습니다.", response));
    }

    @GetMapping("/{riderId}")
    @Operation(summary = "라이더 정보 조회", description = "라이더 ID로 라이더 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<RiderResponse>> getRider(
            @Parameter(description = "라이더 ID") @PathVariable String riderId) {
        log.info("Getting rider info: {}", riderId);
        
        // RiderService에 getRider 메소드 추가 필요
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @GetMapping("/{riderId}/dashboard")
    @Operation(summary = "라이더 대시보드 조회", 
               description = "라이더의 오늘 배송 건수, 평균 배송 시간, 평점 등 대시보드 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<RiderDashboardResponse>> getRiderDashboard(
            @Parameter(description = "라이더 ID") @PathVariable String riderId) {
        log.info("Getting dashboard for rider: {}", riderId);
        
        RiderDashboardResponse dashboard = riderService.getRiderDashboard(riderId);
        
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }

    @PostMapping("/{riderId}/shift/start")
    @Operation(summary = "근무 시작", description = "라이더의 근무를 시작하고 상태를 AVAILABLE로 변경합니다.")
    public ResponseEntity<ApiResponse<Void>> startShift(
            @Parameter(description = "라이더 ID") @PathVariable String riderId) {
        log.info("Rider {} starting shift", riderId);
        
        riderService.startShift(riderId);
        
        return ResponseEntity.ok(ApiResponse.success("근무가 시작되었습니다."));
    }

    @PostMapping("/{riderId}/shift/end")
    @Operation(summary = "근무 종료", description = "라이더의 근무를 종료하고 상태를 OFFLINE으로 변경합니다.")
    public ResponseEntity<ApiResponse<Void>> endShift(
            @Parameter(description = "라이더 ID") @PathVariable String riderId) {
        log.info("Rider {} ending shift", riderId);
        
        riderService.endShift(riderId);
        
        return ResponseEntity.ok(ApiResponse.success("근무가 종료되었습니다."));
    }

    @PutMapping("/{riderId}/location")
    @Operation(summary = "위치 업데이트", 
               description = "라이더의 현재 위치를 업데이트합니다. 실시간으로 관제센터에 브로드캐스트됩니다.")
    public ResponseEntity<ApiResponse<Void>> updateLocation(
            @Parameter(description = "라이더 ID") @PathVariable String riderId,
            @Valid @RequestBody LocationUpdateRequest request) {
        log.debug("Updating location for rider {}: {}, {}", 
                riderId, request.getLatitude(), request.getLongitude());
        
        riderService.updateLocation(riderId, request);
        
        return ResponseEntity.ok(ApiResponse.success("위치가 업데이트되었습니다."));
    }

    @PutMapping("/{riderId}/status")
    @Operation(summary = "상태 변경", description = "라이더의 상태를 변경합니다 (AVAILABLE, BUSY, BREAK, OFFLINE).")
    public ResponseEntity<ApiResponse<Void>> updateStatus(
            @Parameter(description = "라이더 ID") @PathVariable String riderId,
            @Parameter(description = "변경할 상태") @RequestParam RiderStatus status) {
        log.info("Updating rider {} status to {}", riderId, status);
        
        riderService.updateStatus(riderId, status);
        
        return ResponseEntity.ok(ApiResponse.success("상태가 변경되었습니다."));
    }

    @GetMapping("/{riderId}/deliveries")
    @Operation(summary = "내 배송 목록 조회", 
               description = "라이더에게 할당된 배송 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<DeliveryResponse>>> getMyDeliveries(
            @Parameter(description = "라이더 ID") @PathVariable String riderId) {
        log.info("Getting deliveries for rider: {}", riderId);
        
        List<DeliveryResponse> deliveries = deliveryService.getRiderDeliveries(riderId);
        
        return ResponseEntity.ok(ApiResponse.success(deliveries));
    }

    @GetMapping("/{riderId}/deliveries/active")
    @Operation(summary = "진행 중인 배송 조회", 
               description = "라이더가 현재 진행 중인 배송(ASSIGNED, PICKING_UP, IN_TRANSIT)을 조회합니다.")
    public ResponseEntity<ApiResponse<List<DeliveryResponse>>> getActiveDeliveries(
            @Parameter(description = "라이더 ID") @PathVariable String riderId) {
        log.info("Getting active deliveries for rider: {}", riderId);
        
        List<DeliveryResponse> deliveries = deliveryService.getRiderDeliveries(riderId);
        
        // 진행 중인 배송만 필터링
        List<DeliveryResponse> activeDeliveries = deliveries.stream()
                .filter(d -> d.getStatus().name().matches("ASSIGNED|PICKING_UP|IN_TRANSIT"))
                .toList();
        
        return ResponseEntity.ok(ApiResponse.success(activeDeliveries));
    }

    @GetMapping("/available")
    @Operation(summary = "가용 라이더 목록", description = "현재 배송 가능한 라이더 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<RiderResponse>>> getAvailableRiders() {
        log.info("Getting available riders");
        
        List<RiderResponse> riders = riderService.getAvailableRiders();
        
        return ResponseEntity.ok(ApiResponse.success(riders));
    }

    @GetMapping("/active")
    @Operation(summary = "근무 중인 라이더 목록", description = "현재 근무 중인 모든 라이더 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<RiderResponse>>> getActiveRiders() {
        log.info("Getting active riders");
        
        List<RiderResponse> riders = riderService.getActiveRiders();
        
        return ResponseEntity.ok(ApiResponse.success(riders));
    }
}
