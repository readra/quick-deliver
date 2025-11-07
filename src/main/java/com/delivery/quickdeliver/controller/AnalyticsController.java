package com.delivery.quickdeliver.controller;

import com.delivery.quickdeliver.dto.response.ApiResponse;
import com.delivery.quickdeliver.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics API", description = "데이터 분석 및 최적화 API")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    @Operation(summary = "대시보드 분석", 
               description = "전체 시스템의 대시보드 분석 데이터를 조회합니다.")
    public ResponseEntity<ApiResponse<Object>> getDashboardAnalytics() {
        log.info("Getting dashboard analytics");
        
        var analytics = analyticsService.getDashboardAnalytics();
        
        return ResponseEntity.ok(ApiResponse.success(analytics));
    }

    @GetMapping("/efficiency")
    @Operation(summary = "배송 효율성 분석", 
               description = "지정된 기간의 배송 효율성을 분석합니다. 평균 거리, 경로 최적화 점수, 피크 시간 등을 포함합니다.")
    public ResponseEntity<ApiResponse<Object>> getEfficiencyAnalytics(
            @Parameter(description = "시작 날짜") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "종료 날짜") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(7);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }
        
        log.info("Getting efficiency analytics from {} to {}", startDate, endDate);
        
        var analytics = analyticsService.getEfficiencyAnalytics(startDate, endDate);
        
        return ResponseEntity.ok(ApiResponse.success(analytics));
    }

    @GetMapping("/rider/{riderId}")
    @Operation(summary = "라이더 성과 분석", 
               description = "특정 라이더의 성과를 분석합니다. 배송 건수, 평균 시간, 평점, 효율성 점수 등을 포함합니다.")
    public ResponseEntity<ApiResponse<Object>> getRiderPerformanceAnalytics(
            @Parameter(description = "라이더 ID") @PathVariable String riderId,
            @Parameter(description = "시작 날짜") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "종료 날짜") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }
        
        log.info("Getting rider performance analytics for {} from {} to {}", 
                riderId, startDate, endDate);
        
        var analytics = analyticsService.getRiderPerformanceAnalytics(riderId, startDate, endDate);
        
        return ResponseEntity.ok(ApiResponse.success(analytics));
    }

    @GetMapping("/predictions")
    @Operation(summary = "예측 분석", 
               description = "과거 데이터를 기반으로 향후 수요를 예측합니다. 다음 시간 예상 배송 건수, 피크 시간, 권장 라이더 배치 등을 포함합니다.")
    public ResponseEntity<ApiResponse<Object>> getPredictiveAnalytics() {
        log.info("Getting predictive analytics");
        
        var analytics = analyticsService.getPredictiveAnalytics();
        
        return ResponseEntity.ok(ApiResponse.success(analytics));
    }

    @GetMapping("/optimization/suggestions")
    @Operation(summary = "최적화 제안", 
               description = "현재 상황을 분석하여 시스템 최적화 제안을 제공합니다.")
    public ResponseEntity<ApiResponse<Object>> getOptimizationSuggestions() {
        log.info("Getting optimization suggestions");
        
        var suggestions = analyticsService.getOptimizationSuggestions();
        
        return ResponseEntity.ok(ApiResponse.success(suggestions));
    }

    @GetMapping("/reports/daily")
    @Operation(summary = "일일 리포트", 
               description = "지정된 날짜의 일일 운영 리포트를 생성합니다.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDailyReport(
            @Parameter(description = "날짜 (기본값: 오늘)") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime date) {
        
        if (date == null) {
            date = LocalDateTime.now();
        }
        
        log.info("Getting daily report for {}", date.toLocalDate());
        
        LocalDateTime startOfDay = date.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);
        
        Map<String, Object> report = Map.of(
                "date", date.toLocalDate(),
                "efficiency", analyticsService.getEfficiencyAnalytics(startOfDay, endOfDay),
                "dashboard", analyticsService.getDashboardAnalytics()
        );
        
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @GetMapping("/reports/weekly")
    @Operation(summary = "주간 리포트", 
               description = "지난 7일간의 주간 운영 리포트를 생성합니다.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getWeeklyReport() {
        log.info("Getting weekly report");
        
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(7);
        
        Map<String, Object> report = Map.of(
                "period", Map.of("start", startDate, "end", endDate),
                "efficiency", analyticsService.getEfficiencyAnalytics(startDate, endDate),
                "predictions", analyticsService.getPredictiveAnalytics()
        );
        
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @GetMapping("/heatmap/deliveries")
    @Operation(summary = "배송 히트맵 데이터", 
               description = "지역별 배송 밀집도를 히트맵 형태로 제공합니다.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDeliveryHeatmap(
            @Parameter(description = "시작 날짜") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "종료 날짜") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(7);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }
        
        log.info("Getting delivery heatmap from {} to {}", startDate, endDate);
        
        var efficiency = analyticsService.getEfficiencyAnalytics(startDate, endDate);
        
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "period", Map.of("start", startDate, "end", endDate),
                "heatmapData", efficiency
        )));
    }

    @GetMapping("/trends/hourly")
    @Operation(summary = "시간대별 트렌드", 
               description = "24시간 동안의 배송 트렌드를 분석합니다.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getHourlyTrends(
            @Parameter(description = "날짜 (기본값: 오늘)") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime date) {
        
        if (date == null) {
            date = LocalDateTime.now();
        }
        
        log.info("Getting hourly trends for {}", date.toLocalDate());
        
        LocalDateTime startOfDay = date.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);
        
        var efficiency = analyticsService.getEfficiencyAnalytics(startOfDay, endOfDay);
        
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "date", date.toLocalDate(),
                "trends", efficiency
        )));
    }

    @GetMapping("/comparison/riders")
    @Operation(summary = "라이더 비교 분석", 
               description = "여러 라이더의 성과를 비교 분석합니다.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> compareRiders(
            @Parameter(description = "라이더 ID 목록 (콤마로 구분)") @RequestParam String riderIds,
            @Parameter(description = "시작 날짜") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "종료 날짜") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }
        
        log.info("Comparing riders: {}", riderIds);
        
        String[] ids = riderIds.split(",");
        Map<String, Object> comparison = new java.util.HashMap<>();
        
        for (String riderId : ids) {
            var analytics = analyticsService.getRiderPerformanceAnalytics(
                    riderId.trim(), startDate, endDate);
            comparison.put(riderId.trim(), analytics);
        }
        
        return ResponseEntity.ok(ApiResponse.success(comparison));
    }

    @GetMapping("/metrics/realtime")
    @Operation(summary = "실시간 메트릭", 
               description = "시스템의 실시간 성능 메트릭을 조회합니다.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRealtimeMetrics() {
        log.info("Getting realtime metrics");
        
        var dashboard = analyticsService.getDashboardAnalytics();
        
        Map<String, Object> metrics = Map.of(
                "timestamp", LocalDateTime.now(),
                "metrics", dashboard
        );
        
        return ResponseEntity.ok(ApiResponse.success(metrics));
    }
}
