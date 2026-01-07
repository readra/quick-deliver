package com.delivery.quickdeliver.service;

import com.delivery.quickdeliver.domain.entity.Rider;
import com.delivery.quickdeliver.domain.enums.RiderStatus;
import com.delivery.quickdeliver.dto.route.RiderRoute;
import com.delivery.quickdeliver.dto.route.RouteData;
import com.delivery.quickdeliver.dto.route.Waypoint;
import com.delivery.quickdeliver.repository.RiderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationSimulatorService {

    private final RiderRepository riderRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;
    
    private Map<String, RiderRoute> activeRoutes = new HashMap<>();
    private RouteData routeData;

    @PostConstruct
    public void init() {
        loadRouteData();
        initializeActiveRoutes();
    }

    private void loadRouteData() {
        try {
            ClassPathResource resource = new ClassPathResource("data/rider-routes.json");
            routeData = objectMapper.readValue(resource.getInputStream(), RouteData.class);
            log.info("Loaded {} routes from rider-routes.json", routeData.getRoutes().size());
        } catch (IOException e) {
            log.error("Failed to load route data", e);
            routeData = new RouteData();
        }
    }

    private void initializeActiveRoutes() {
        if (routeData == null || routeData.getRoutes() == null) {
            return;
        }
        
        // DELIVERING 상태인 라이더들의 경로 활성화
        for (RiderRoute route : routeData.getRoutes()) {
            Rider rider = riderRepository.findByRiderId(route.getRiderId()).orElse(null);
            if (rider != null && rider.getStatus() == RiderStatus.DELIVERING) {
                route.start();
                activeRoutes.put(route.getRiderId(), route);
                log.info("Started route simulation for rider: {} - {}", route.getRiderId(), route.getRouteName());
            }
        }
    }

    /**
     * 5초마다 라이더 위치 업데이트
     */
    @Scheduled(fixedRate = 5000)
    @Transactional
    public void updateRiderLocations() {
        if (activeRoutes.isEmpty()) {
            return;
        }

        for (Map.Entry<String, RiderRoute> entry : activeRoutes.entrySet()) {
            String riderId = entry.getKey();
            RiderRoute route = entry.getValue();
            
            // 경로 진행 상태 업데이트
            route.updateProgress();
            
            Waypoint currentWaypoint = route.getCurrentWaypoint();
            if (currentWaypoint == null) {
                continue;
            }
            
            // DB 업데이트
            Rider rider = riderRepository.findByRiderId(riderId).orElse(null);
            if (rider != null) {
                rider.setCurrentLatitude(currentWaypoint.getLatitude());
                rider.setCurrentLongitude(currentWaypoint.getLongitude());
                rider.setLastLocationUpdate(LocalDateTime.now());
                riderRepository.save(rider);
                
                // WebSocket으로 브로드캐스트
                Map<String, Object> locationUpdate = new HashMap<>();
                locationUpdate.put("riderId", riderId);
                locationUpdate.put("name", rider.getName());
                locationUpdate.put("latitude", currentWaypoint.getLatitude());
                locationUpdate.put("longitude", currentWaypoint.getLongitude());
                locationUpdate.put("status", rider.getStatus().name());
                locationUpdate.put("description", currentWaypoint.getDescription());
                locationUpdate.put("timestamp", System.currentTimeMillis());
                
                messagingTemplate.convertAndSend("/topic/monitoring/riders", locationUpdate);
                
                log.debug("Updated location for {}: {} - {}", 
                    riderId, currentWaypoint.getDescription(), 
                    String.format("%.4f, %.4f", currentWaypoint.getLatitude(), currentWaypoint.getLongitude()));
            }
            
            // 경로 완료 시 제거
            if (route.isCompleted()) {
                log.info("Route completed for rider: {} - {}", riderId, route.getRouteName());
                activeRoutes.remove(riderId);
            }
        }
    }

    /**
     * 라이더의 배달 시작 시 경로 활성화
     */
    public void startRoute(String riderId) {
        if (routeData == null || routeData.getRoutes() == null) {
            return;
        }
        
        // 해당 라이더의 경로 찾기
        RiderRoute route = routeData.getRoutes().stream()
                .filter(r -> r.getRiderId().equals(riderId))
                .filter(r -> !activeRoutes.containsKey(riderId))
                .findFirst()
                .orElse(null);
        
        if (route != null) {
            route.start();
            activeRoutes.put(riderId, route);
            log.info("Manually started route for rider: {} - {}", riderId, route.getRouteName());
        }
    }

    /**
     * 라이더의 경로 중지
     */
    public void stopRoute(String riderId) {
        RiderRoute removed = activeRoutes.remove(riderId);
        if (removed != null) {
            log.info("Stopped route for rider: {} - {}", riderId, removed.getRouteName());
        }
    }

    /**
     * 활성 경로 수 조회
     */
    public int getActiveRouteCount() {
        return activeRoutes.size();
    }

    /**
     * 경로 재시작 (테스트용)
     */
    public void restartAllRoutes() {
        activeRoutes.clear();
        initializeActiveRoutes();
        log.info("Restarted all routes");
    }
}
