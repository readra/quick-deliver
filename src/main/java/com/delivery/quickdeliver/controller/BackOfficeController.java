package com.delivery.quickdeliver.controller;

import com.delivery.quickdeliver.domain.enums.DeliveryStatus;
import com.delivery.quickdeliver.domain.enums.RiderStatus;
import com.delivery.quickdeliver.dto.response.ApiResponse;
import com.delivery.quickdeliver.dto.response.DeliveryResponse;
import com.delivery.quickdeliver.dto.response.RiderResponse;
import com.delivery.quickdeliver.service.BackOfficeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/backoffice")
@RequiredArgsConstructor
@Tag(name = "BackOffice API", description = "관제센터 및 관리자 API")
public class BackOfficeController {

    private final BackOfficeService backOfficeService;

    @GetMapping("/dashboard")
    @Operation(summary = "전체 대시보드", 
               description = "배송 현황, 라이더 현황, 실시간 통계 등 전체 대시보드 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboard() {
        log.info("Getting backoffice dashboard");
        
        Map<String, Object> dashboard = backOfficeService.getDashboard();
        
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }

    // ==================== 라이더 관리 ====================

    @GetMapping("/riders")
    @Operation(summary = "전체 라이더 목록", description = "모든 라이더 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<RiderResponse>>> getAllRiders(
            @Parameter(description = "상태 필터") @RequestParam(required = false) RiderStatus status) {
        log.info("Getting all riders, status filter: {}", status);
        
        List<RiderResponse> riders = backOfficeService.getAllRiders(status);
        
        return ResponseEntity.ok(ApiResponse.success(riders));
    }

    @GetMapping("/riders/{riderId}")
    @Operation(summary = "라이더 상세 정보", description = "특정 라이더의 상세 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRiderDetail(
            @Parameter(description = "라이더 ID") @PathVariable String riderId) {
        log.info("Getting rider detail: {}", riderId);
        
        Map<String, Object> riderDetail = backOfficeService.getRiderDetail(riderId);
        
        return ResponseEntity.ok(ApiResponse.success(riderDetail));
    }

    @PutMapping("/riders/{riderId}/activate")
    @Operation(summary = "라이더 활성화", description = "라이더 계정을 활성화합니다.")
    public ResponseEntity<ApiResponse<Void>> activateRider(
            @Parameter(description = "라이더 ID") @PathVariable String riderId) {
        log.info("Activating rider: {}", riderId);
        
        backOfficeService.activateRider(riderId);
        
        return ResponseEntity.ok(ApiResponse.success("라이더가 활성화되었습니다."));
    }

    @PutMapping("/riders/{riderId}/deactivate")
    @Operation(summary = "라이더 비활성화", description = "라이더 계정을 비활성화합니다.")
    public ResponseEntity<ApiResponse<Void>> deactivateRider(
            @Parameter(description = "라이더 ID") @PathVariable String riderId) {
        log.info("Deactivating rider: {}", riderId);
        
        backOfficeService.deactivateRider(riderId);
        
        return ResponseEntity.ok(ApiResponse.success("라이더가 비활성화되었습니다."));
    }

    @GetMapping("/riders/location/map")
    @Operation(summary = "라이더 위치 지도", 
               description = "근무 중인 모든 라이더의 실시간 위치 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getRidersLocationMap() {
        log.info("Getting riders location map");
        
        List<Map<String, Object>> locations = backOfficeService.getRidersLocationMap();
        
        return ResponseEntity.ok(ApiResponse.success(locations));
    }

    // ==================== 배송 관리 ====================

    @GetMapping("/deliveries")
    @Operation(summary = "전체 배송 목록", 
               description = "모든 배송 목록을 조회합니다. 상태, 날짜로 필터링 가능합니다.")
    public ResponseEntity<ApiResponse<List<DeliveryResponse>>> getAllDeliveries(
            @Parameter(description = "상태 필터") @RequestParam(required = false) DeliveryStatus status,
            @Parameter(description = "시작 날짜") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "종료 날짜") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("Getting all deliveries, status: {}, startDate: {}, endDate: {}", 
                status, startDate, endDate);
        
        List<DeliveryResponse> deliveries = backOfficeService.getAllDeliveries(status, startDate, endDate);
        
        return ResponseEntity.ok(ApiResponse.success(deliveries));
    }

    @GetMapping("/deliveries/{deliveryId}")
    @Operation(summary = "배송 상세 정보", description = "특정 배송의 상세 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDeliveryDetail(
            @Parameter(description = "배송 ID") @PathVariable String deliveryId) {
        log.info("Getting delivery detail: {}", deliveryId);
        
        Map<String, Object> deliveryDetail = backOfficeService.getDeliveryDetail(deliveryId);
        
        return ResponseEntity.ok(ApiResponse.success(deliveryDetail));
    }

    @PostMapping("/deliveries/{deliveryId}/assign/{riderId}")
    @Operation(summary = "라이더 수동 배정", 
               description = "관리자가 특정 배송을 특정 라이더에게 수동으로 배정합니다.")
    public ResponseEntity<ApiResponse<DeliveryResponse>> manualAssignDelivery(
            @Parameter(description = "배송 ID") @PathVariable String deliveryId,
            @Parameter(description = "라이더 ID") @PathVariable String riderId) {
        log.info("Manually assigning delivery {} to rider {}", deliveryId, riderId);
        
        DeliveryResponse delivery = backOfficeService.manualAssignDelivery(deliveryId, riderId);
        
        return ResponseEntity.ok(ApiResponse.success("배송이 배정되었습니다.", delivery));
    }

    @PostMapping("/deliveries/{deliveryId}/reassign")
    @Operation(summary = "라이더 재배정", 
               description = "현재 배정된 라이더를 해제하고 다른 라이더에게 재배정합니다.")
    public ResponseEntity<ApiResponse<DeliveryResponse>> reassignDelivery(
            @Parameter(description = "배송 ID") @PathVariable String deliveryId,
            @Parameter(description = "새 라이더 ID (없으면 자동 배정)") 
            @RequestParam(required = false) String newRiderId) {
        log.info("Reassigning delivery {} to rider {}", deliveryId, newRiderId);
        
        DeliveryResponse delivery = backOfficeService.reassignDelivery(deliveryId, newRiderId);
        
        return ResponseEntity.ok(ApiResponse.success("배송이 재배정되었습니다.", delivery));
    }

    @GetMapping("/deliveries/delayed")
    @Operation(summary = "지연 배송 목록", 
               description = "예상 배송 시간을 초과한 지연 배송 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<DeliveryResponse>>> getDelayedDeliveries() {
        log.info("Getting delayed deliveries");
        
        List<DeliveryResponse> deliveries = backOfficeService.getDelayedDeliveries();
        
        return ResponseEntity.ok(ApiResponse.success(deliveries));
    }

    @GetMapping("/deliveries/statistics")
    @Operation(summary = "배송 통계", 
               description = "기간별 배송 통계를 조회합니다 (완료율, 평균 시간, 취소율 등).")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDeliveryStatistics(
            @Parameter(description = "시작 날짜") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "종료 날짜") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("Getting delivery statistics from {} to {}", startDate, endDate);
        
        Map<String, Object> statistics = backOfficeService.getDeliveryStatistics(startDate, endDate);
        
        return ResponseEntity.ok(ApiResponse.success(statistics));
    }

    // ==================== 모니터링 ====================

    @GetMapping("/monitoring/realtime")
    @Operation(summary = "실시간 모니터링", 
               description = "현재 진행 중인 배송, 근무 중인 라이더 등 실시간 현황을 조회합니다.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRealtimeMonitoring() {
        log.info("Getting realtime monitoring data");
        
        Map<String, Object> monitoring = backOfficeService.getRealtimeMonitoring();
        
        return ResponseEntity.ok(ApiResponse.success(monitoring));
    }

    @GetMapping("/monitoring/alerts")
    @Operation(summary = "알림/경고 목록", 
               description = "지연 배송, 라이더 부족 등 주의가 필요한 알림 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAlerts() {
        log.info("Getting system alerts");
        
        List<Map<String, Object>> alerts = backOfficeService.getSystemAlerts();
        
        return ResponseEntity.ok(ApiResponse.success(alerts));
    }

    // ==================== 성과 관리 ====================

    @GetMapping("/performance/riders")
    @Operation(summary = "라이더 성과 순위", 
               description = "기간별 라이더 성과를 순위별로 조회합니다.")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getRidersPerformanceRanking(
            @Parameter(description = "시작 날짜") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "종료 날짜") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("Getting riders performance ranking from {} to {}", startDate, endDate);
        
        List<Map<String, Object>> ranking = backOfficeService.getRidersPerformanceRanking(
                startDate, endDate);
        
        return ResponseEntity.ok(ApiResponse.success(ranking));
    }

    @GetMapping("/performance/summary")
    @Operation(summary = "전체 성과 요약", 
               description = "전체 시스템의 성과 요약 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPerformanceSummary(
            @Parameter(description = "시작 날짜") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "종료 날짜") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("Getting performance summary from {} to {}", startDate, endDate);
        
        Map<String, Object> summary = backOfficeService.getPerformanceSummary(startDate, endDate);
        
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    // ==================== 설정 및 관리 ====================

    @GetMapping("/settings/operating-hours")
    @Operation(summary = "운영 시간 조회", description = "시스템 운영 시간 설정을 조회합니다.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOperatingHours() {
        log.info("Getting operating hours");
        
        Map<String, Object> operatingHours = backOfficeService.getOperatingHours();
        
        return ResponseEntity.ok(ApiResponse.success(operatingHours));
    }

    @PutMapping("/settings/operating-hours")
    @Operation(summary = "운영 시간 설정", description = "시스템 운영 시간을 설정합니다.")
    public ResponseEntity<ApiResponse<Void>> updateOperatingHours(
            @RequestBody Map<String, Object> operatingHours) {
        log.info("Updating operating hours: {}", operatingHours);
        
        backOfficeService.updateOperatingHours(operatingHours);
        
        return ResponseEntity.ok(ApiResponse.success("운영 시간이 업데이트되었습니다."));
    }

    @GetMapping("/export/deliveries")
    @Operation(summary = "배송 데이터 내보내기", 
               description = "지정된 기간의 배송 데이터를 CSV 형식으로 내보냅니다.")
    public ResponseEntity<ApiResponse<String>> exportDeliveries(
            @Parameter(description = "시작 날짜") @RequestParam 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "종료 날짜") @RequestParam 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("Exporting deliveries from {} to {}", startDate, endDate);
        
        String csvData = backOfficeService.exportDeliveriesCSV(startDate, endDate);
        
        return ResponseEntity.ok(ApiResponse.success("데이터 내보내기 완료", csvData));
    }
}
