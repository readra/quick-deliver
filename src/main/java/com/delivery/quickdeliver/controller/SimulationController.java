package com.delivery.quickdeliver.controller;

import com.delivery.quickdeliver.dto.response.ApiResponse;
import com.delivery.quickdeliver.service.LocationSimulatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/simulation")
@RequiredArgsConstructor
@Tag(name = "Simulation API", description = "위치 시뮬레이션 관리 API (개발/테스트용)")
public class SimulationController {

    private final LocationSimulatorService locationSimulatorService;

    @PostMapping("/routes/{riderId}/start")
    @Operation(summary = "라이더 경로 시뮬레이션 시작", description = "특정 라이더의 이동 경로 시뮬레이션을 수동으로 시작합니다.")
    public ResponseEntity<ApiResponse<Void>> startRoute(@PathVariable String riderId) {
        log.info("Starting route simulation for rider: {}", riderId);
        locationSimulatorService.startRoute(riderId);
        return ResponseEntity.ok(ApiResponse.success("경로 시뮬레이션이 시작되었습니다."));
    }

    @PostMapping("/routes/{riderId}/stop")
    @Operation(summary = "라이더 경로 시뮬레이션 중지", description = "특정 라이더의 이동 경로 시뮬레이션을 중지합니다.")
    public ResponseEntity<ApiResponse<Void>> stopRoute(@PathVariable String riderId) {
        log.info("Stopping route simulation for rider: {}", riderId);
        locationSimulatorService.stopRoute(riderId);
        return ResponseEntity.ok(ApiResponse.success("경로 시뮬레이션이 중지되었습니다."));
    }

    @PostMapping("/routes/restart")
    @Operation(summary = "모든 경로 재시작", description = "모든 활성 경로를 재시작합니다.")
    public ResponseEntity<ApiResponse<Void>> restartAllRoutes() {
        log.info("Restarting all route simulations");
        locationSimulatorService.restartAllRoutes();
        return ResponseEntity.ok(ApiResponse.success("모든 경로가 재시작되었습니다."));
    }

    @GetMapping("/routes/status")
    @Operation(summary = "시뮬레이션 상태 조회", description = "현재 활성화된 경로 시뮬레이션 상태를 조회합니다.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSimulationStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("activeRoutes", locationSimulatorService.getActiveRouteCount());
        status.put("isRunning", locationSimulatorService.getActiveRouteCount() > 0);
        
        return ResponseEntity.ok(ApiResponse.success(status));
    }
}
