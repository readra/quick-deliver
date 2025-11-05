package com.delivery.quickdeliver.service;

import com.delivery.quickdeliver.domain.entity.Address;
import com.delivery.quickdeliver.domain.entity.Delivery;
import com.delivery.quickdeliver.dto.response.OptimizedRouteResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RouteOptimizationService {

    /**
     * 여러 배송지에 대한 최적 경로 계산 (TSP - Traveling Salesman Problem)
     */
    public OptimizedRouteResponse optimizeMultipleDeliveries(
            Address startPoint, 
            List<Delivery> deliveries) {
        
        log.info("Optimizing route for {} deliveries", deliveries.size());

        if (deliveries.isEmpty()) {
            return OptimizedRouteResponse.builder()
                    .totalDistance(0.0)
                    .estimatedTime(0)
                    .waypoints(Collections.emptyList())
                    .build();
        }

        // 간단한 Nearest Neighbor 알고리즘 구현
        List<OptimizedRouteResponse.Waypoint> waypoints = new ArrayList<>();
        Set<Delivery> visited = new HashSet<>();
        Address currentLocation = startPoint;
        double totalDistance = 0;

        while (visited.size() < deliveries.size()) {
            Delivery nearest = findNearestDelivery(currentLocation, deliveries, visited);
            if (nearest == null) break;

            // 픽업 지점으로 이동
            double pickupDistance = calculateDistance(
                    currentLocation, nearest.getPickupAddress());
            totalDistance += pickupDistance;

            waypoints.add(OptimizedRouteResponse.Waypoint.builder()
                    .deliveryId(nearest.getDeliveryId())
                    .type("PICKUP")
                    .address(nearest.getPickupAddress())
                    .estimatedArrival(calculateEstimatedTime(pickupDistance))
                    .build());

            // 배송 지점으로 이동
            double deliveryDistance = calculateDistance(
                    nearest.getPickupAddress(), nearest.getDeliveryAddress());
            totalDistance += deliveryDistance;

            waypoints.add(OptimizedRouteResponse.Waypoint.builder()
                    .deliveryId(nearest.getDeliveryId())
                    .type("DELIVERY")
                    .address(nearest.getDeliveryAddress())
                    .estimatedArrival(calculateEstimatedTime(deliveryDistance))
                    .build());

            currentLocation = nearest.getDeliveryAddress();
            visited.add(nearest);
        }

        return OptimizedRouteResponse.builder()
                .totalDistance(totalDistance)
                .estimatedTime(calculateTotalTime(totalDistance))
                .waypoints(waypoints)
                .optimizationScore(calculateOptimizationScore(totalDistance, deliveries.size()))
                .build();
    }

    /**
     * 단일 배송에 대한 최적 경로 계산
     */
    public String calculateOptimalRoute(Address pickup, Address delivery) {
        // 실제로는 외부 지도 API (Google Maps, Kakao Maps 등)를 호출
        // 여기서는 간단한 mock 데이터 반환
        Map<String, Object> route = new HashMap<>();
        route.put("distance", calculateDistance(pickup, delivery));
        route.put("duration", calculateTotalTime(calculateDistance(pickup, delivery)));
        route.put("polyline", generateMockPolyline(pickup, delivery));
        
        return convertToJson(route);
    }

    /**
     * 두 지점 간 거리 계산 (Haversine Formula)
     */
    public double calculateDistance(Address from, Address to) {
        if (from == null || to == null || 
            from.getLatitude() == null || from.getLongitude() == null ||
            to.getLatitude() == null || to.getLongitude() == null) {
            return 0.0;
        }

        double R = 6371; // 지구 반경 (km)
        double dLat = Math.toRadians(to.getLatitude() - from.getLatitude());
        double dLon = Math.toRadians(to.getLongitude() - from.getLongitude());
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(from.getLatitude())) * 
                Math.cos(Math.toRadians(to.getLatitude())) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    /**
     * 배송 밀집도 분석
     */
    public Map<String, Object> analyzeDeliveryDensity(List<Delivery> deliveries) {
        Map<String, Object> analysis = new HashMap<>();
        
        // 구역별 배송 건수 계산
        Map<String, Integer> zoneCount = new HashMap<>();
        for (Delivery delivery : deliveries) {
            String zone = getZone(delivery.getDeliveryAddress());
            zoneCount.put(zone, zoneCount.getOrDefault(zone, 0) + 1);
        }
        
        // 핫스팟 식별
        List<String> hotspots = zoneCount.entrySet().stream()
                .filter(e -> e.getValue() > 5)
                .map(Map.Entry::getKey)
                .toList();
        
        analysis.put("zoneDistribution", zoneCount);
        analysis.put("hotspots", hotspots);
        analysis.put("totalDeliveries", deliveries.size());
        analysis.put("averageDensity", deliveries.size() / (double) zoneCount.size());
        
        return analysis;
    }

    private Delivery findNearestDelivery(Address currentLocation, 
                                         List<Delivery> deliveries, 
                                         Set<Delivery> visited) {
        return deliveries.stream()
                .filter(d -> !visited.contains(d))
                .min((d1, d2) -> {
                    double dist1 = calculateDistance(currentLocation, d1.getPickupAddress());
                    double dist2 = calculateDistance(currentLocation, d2.getPickupAddress());
                    return Double.compare(dist1, dist2);
                })
                .orElse(null);
    }

    private int calculateEstimatedTime(double distance) {
        // 평균 속도 30km/h 가정
        return (int) (distance / 30 * 60); // 분 단위
    }

    private int calculateTotalTime(double totalDistance) {
        // 거리 + 각 지점에서의 처리 시간 (5분)
        return calculateEstimatedTime(totalDistance) + 10;
    }

    private double calculateOptimizationScore(double totalDistance, int deliveryCount) {
        // 최적화 점수 계산 (0-100)
        double avgDistancePerDelivery = totalDistance / deliveryCount;
        if (avgDistancePerDelivery < 2) return 100;
        if (avgDistancePerDelivery < 5) return 80;
        if (avgDistancePerDelivery < 10) return 60;
        return 40;
    }

    private String getZone(Address address) {
        // 주소를 구역으로 변환 (실제로는 행정구역 API 사용)
        if (address == null || address.getAddress() == null) return "Unknown";
        String[] parts = address.getAddress().split(" ");
        return parts.length > 1 ? parts[1] : "Zone1";
    }

    private String generateMockPolyline(Address from, Address to) {
        // 실제로는 지도 API에서 받은 polyline 사용
        return String.format("path:[%f,%f]->[%f,%f]", 
                from.getLatitude(), from.getLongitude(),
                to.getLatitude(), to.getLongitude());
    }

    private String convertToJson(Map<String, Object> data) {
        // 간단한 JSON 변환 (실제로는 ObjectMapper 사용)
        StringBuilder json = new StringBuilder("{");
        data.forEach((key, value) -> 
                json.append("\"").append(key).append("\":\"")
                   .append(value).append("\","));
        if (json.length() > 1) {
            json.setLength(json.length() - 1);
        }
        json.append("}");
        return json.toString();
    }
}
