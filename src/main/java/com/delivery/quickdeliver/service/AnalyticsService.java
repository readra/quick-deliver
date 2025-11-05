package com.delivery.quickdeliver.service;

import com.delivery.quickdeliver.domain.entity.Delivery;
import com.delivery.quickdeliver.domain.entity.Rider;
import com.delivery.quickdeliver.domain.entity.RiderPerformance;
import com.delivery.quickdeliver.domain.enums.DeliveryStatus;
import com.delivery.quickdeliver.dto.response.AnalyticsResponse;
import com.delivery.quickdeliver.repository.DeliveryRepository;
import com.delivery.quickdeliver.repository.RiderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final DeliveryRepository deliveryRepository;
    private final RiderRepository riderRepository;
    private final RouteOptimizationService routeOptimizationService;

    /**
     * 전체 대시보드 분석 데이터
     */
    public AnalyticsResponse.DashboardAnalytics getDashboardAnalytics() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = now.toLocalDate().atStartOfDay();
        LocalDateTime weekStart = now.minusDays(7);

        // 오늘의 통계
        List<Delivery> todayDeliveries = deliveryRepository.findDeliveriesBetweenDates(
                todayStart, now);
        
        // 주간 통계
        List<Delivery> weekDeliveries = deliveryRepository.findDeliveriesBetweenDates(
                weekStart, now);

        return AnalyticsResponse.DashboardAnalytics.builder()
                .totalDeliveriesToday(todayDeliveries.size())
                .totalDeliveriesWeek(weekDeliveries.size())
                .completedDeliveries(countByStatus(todayDeliveries, DeliveryStatus.DELIVERED))
                .pendingDeliveries(countByStatus(todayDeliveries, DeliveryStatus.PENDING))
                .activeRiders(riderRepository.findActiveRiders().size())
                .averageDeliveryTime(calculateAverageDeliveryTime(todayDeliveries))
                .onTimeDeliveryRate(calculateOnTimeRate(todayDeliveries))
                .customerSatisfaction(calculateSatisfactionScore(weekDeliveries))
                .build();
    }

    /**
     * 배송 효율성 분석
     */
    public AnalyticsResponse.EfficiencyAnalytics getEfficiencyAnalytics(
            LocalDateTime startDate, LocalDateTime endDate) {
        
        List<Delivery> deliveries = deliveryRepository.findDeliveriesBetweenDates(
                startDate, endDate);
        
        Map<String, Object> densityAnalysis = routeOptimizationService
                .analyzeDeliveryDensity(deliveries);

        return AnalyticsResponse.EfficiencyAnalytics.builder()
                .totalDeliveries(deliveries.size())
                .averageDistancePerDelivery(calculateAverageDistance(deliveries))
                .routeOptimizationScore(calculateRouteOptimizationScore(deliveries))
                .peakHours(identifyPeakHours(deliveries))
                .deliveryDensityMap(densityAnalysis)
                .recommendedRiderCount(calculateOptimalRiderCount(deliveries))
                .build();
    }

    /**
     * 라이더 성과 분석
     */
    public AnalyticsResponse.RiderPerformanceAnalytics getRiderPerformanceAnalytics(
            String riderId, LocalDateTime startDate, LocalDateTime endDate) {
        
        Rider rider = riderRepository.findByRiderId(riderId)
                .orElseThrow(() -> new RuntimeException("Rider not found"));

        List<Delivery> riderDeliveries = deliveryRepository.findByRider(rider).stream()
                .filter(d -> d.getActualDeliveryTime() != null &&
                        d.getActualDeliveryTime().isAfter(startDate) &&
                        d.getActualDeliveryTime().isBefore(endDate))
                .collect(Collectors.toList());

        return AnalyticsResponse.RiderPerformanceAnalytics.builder()
                .riderId(riderId)
                .riderName(rider.getName())
                .totalDeliveries(riderDeliveries.size())
                .averageDeliveryTime(calculateAverageDeliveryTime(riderDeliveries))
                .totalDistance(calculateTotalDistance(riderDeliveries))
                .averageRating(rider.getAverageRating())
                .onTimeRate(calculateOnTimeRate(riderDeliveries))
                .efficiencyScore(calculateRiderEfficiency(rider, riderDeliveries))
                .dailyPerformance(getDailyPerformance(rider))
                .build();
    }

    /**
     * 예측 분석 - 수요 예측
     */
    public AnalyticsResponse.PredictiveAnalytics getPredictiveAnalytics() {
        // 과거 4주 데이터 기반 예측
        LocalDateTime now = LocalDateTime.now();
        List<Delivery> historicalData = deliveryRepository.findDeliveriesBetweenDates(
                now.minusWeeks(4), now);

        Map<Integer, Long> hourlyPattern = historicalData.stream()
                .collect(Collectors.groupingBy(
                        d -> d.getRequestedTime().getHour(),
                        Collectors.counting()
                ));

        Map<String, Long> dailyPattern = historicalData.stream()
                .collect(Collectors.groupingBy(
                        d -> d.getRequestedTime().getDayOfWeek().toString(),
                        Collectors.counting()
                ));

        // 다음 시간 예측
        int currentHour = now.getHour();
        long predictedNextHour = hourlyPattern.getOrDefault(currentHour + 1, 0L) / 28; // 4주 평균

        return AnalyticsResponse.PredictiveAnalytics.builder()
                .predictedDeliveriesNextHour(predictedNextHour)
                .hourlyPattern(hourlyPattern)
                .dailyPattern(dailyPattern)
                .recommendedRiderSchedule(generateRiderSchedule(hourlyPattern))
                .expectedPeakTime(findPeakTime(hourlyPattern))
                .build();
    }

    /**
     * 최적화 제안
     */
    public AnalyticsResponse.OptimizationSuggestions getOptimizationSuggestions() {
        List<String> suggestions = new ArrayList<>();
        
        // 현재 상황 분석
        List<Delivery> pendingDeliveries = deliveryRepository.findPendingDeliveries();
        List<Rider> availableRiders = riderRepository.findByStatus(com.delivery.quickdeliver.domain.enums.RiderStatus.AVAILABLE);
        List<Delivery> delayedDeliveries = deliveryRepository.findDelayedDeliveries(LocalDateTime.now());

        // 제안 생성
        if (pendingDeliveries.size() > availableRiders.size() * 3) {
            suggestions.add("라이더 부족: 추가 라이더 호출이 필요합니다.");
        }

        if (delayedDeliveries.size() > 0) {
            suggestions.add(String.format("%d건의 지연 배송이 있습니다. 우선순위 재조정이 필요합니다.",
                    delayedDeliveries.size()));
        }

        // 구역별 분석
        Map<String, Object> densityAnalysis = routeOptimizationService
                .analyzeDeliveryDensity(pendingDeliveries);
        
        @SuppressWarnings("unchecked")
        List<String> hotspots = (List<String>) densityAnalysis.get("hotspots");
        if (!hotspots.isEmpty()) {
            suggestions.add(String.format("핫스팟 지역: %s - 해당 지역에 라이더 집중 배치 권장",
                    String.join(", ", hotspots)));
        }

        return AnalyticsResponse.OptimizationSuggestions.builder()
                .suggestions(suggestions)
                .currentLoad(calculateSystemLoad(pendingDeliveries.size(), availableRiders.size()))
                .recommendedActions(generateRecommendedActions())
                .build();
    }

    // Helper methods
    private long countByStatus(List<Delivery> deliveries, DeliveryStatus status) {
        return deliveries.stream()
                .filter(d -> d.getStatus() == status)
                .count();
    }

    private double calculateAverageDeliveryTime(List<Delivery> deliveries) {
        return deliveries.stream()
                .filter(d -> d.getActualDeliveryTime() != null && d.getRequestedTime() != null)
                .mapToLong(d -> ChronoUnit.MINUTES.between(d.getRequestedTime(), d.getActualDeliveryTime()))
                .average()
                .orElse(0.0);
    }

    private double calculateOnTimeRate(List<Delivery> deliveries) {
        if (deliveries.isEmpty()) return 100.0;
        
        long onTime = deliveries.stream()
                .filter(d -> d.getActualDeliveryTime() != null && 
                        d.getEstimatedDeliveryTime() != null &&
                        !d.getActualDeliveryTime().isAfter(d.getEstimatedDeliveryTime()))
                .count();
        
        return (double) onTime / deliveries.size() * 100;
    }

    private double calculateSatisfactionScore(List<Delivery> deliveries) {
        return deliveries.stream()
                .filter(d -> d.getRating() != null)
                .mapToInt(Delivery::getRating)
                .average()
                .orElse(5.0) * 20; // Convert 5-star to 100-point scale
    }

    private double calculateAverageDistance(List<Delivery> deliveries) {
        return deliveries.stream()
                .filter(d -> d.getActualDistance() != null)
                .mapToDouble(Delivery::getActualDistance)
                .average()
                .orElse(0.0);
    }

    private double calculateRouteOptimizationScore(List<Delivery> deliveries) {
        // 실제 거리 vs 예상 거리 비교
        double actualTotal = deliveries.stream()
                .filter(d -> d.getActualDistance() != null)
                .mapToDouble(Delivery::getActualDistance)
                .sum();
        
        double estimatedTotal = deliveries.stream()
                .filter(d -> d.getEstimatedDistance() != null)
                .mapToDouble(Delivery::getEstimatedDistance)
                .sum();
        
        if (estimatedTotal == 0) return 0;
        return Math.min(100, (estimatedTotal / actualTotal) * 100);
    }

    private List<String> identifyPeakHours(List<Delivery> deliveries) {
        Map<Integer, Long> hourCounts = deliveries.stream()
                .collect(Collectors.groupingBy(
                        d -> d.getRequestedTime().getHour(),
                        Collectors.counting()
                ));
        
        long avgCount = hourCounts.values().stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0);
        
        return hourCounts.entrySet().stream()
                .filter(e -> e.getValue() > avgCount * 1.5)
                .map(e -> String.format("%02d:00-%02d:00", e.getKey(), e.getKey() + 1))
                .collect(Collectors.toList());
    }

    private int calculateOptimalRiderCount(List<Delivery> deliveries) {
        // 시간당 평균 배송 수 / 라이더당 시간당 처리 가능 배송 수
        double avgDeliveriesPerHour = deliveries.size() / 24.0;
        double deliveriesPerRiderPerHour = 3.0; // 가정: 라이더당 시간당 3건
        return (int) Math.ceil(avgDeliveriesPerHour / deliveriesPerRiderPerHour * 1.2); // 20% 버퍼
    }

    private double calculateTotalDistance(List<Delivery> deliveries) {
        return deliveries.stream()
                .filter(d -> d.getActualDistance() != null)
                .mapToDouble(Delivery::getActualDistance)
                .sum();
    }

    private double calculateRiderEfficiency(Rider rider, List<Delivery> deliveries) {
        if (deliveries.isEmpty()) return 0;
        
        double avgTime = calculateAverageDeliveryTime(deliveries);
        double onTimeRate = calculateOnTimeRate(deliveries);
        double rating = rider.getAverageRating() * 20; // Convert to 100 scale
        
        return (100 - avgTime/2 + onTimeRate + rating) / 3; // Weighted average
    }

    private List<AnalyticsResponse.DailyPerformance> getDailyPerformance(Rider rider) {
        return rider.getPerformances().stream()
                .map(p -> AnalyticsResponse.DailyPerformance.builder()
                        .date(p.getDate())
                        .deliveries(p.getTotalDeliveries())
                        .averageTime(p.getAverageDeliveryTime())
                        .efficiencyScore(p.getDeliveryEfficiencyScore())
                        .build())
                .collect(Collectors.toList());
    }

    private Map<Integer, Integer> generateRiderSchedule(Map<Integer, Long> hourlyPattern) {
        Map<Integer, Integer> schedule = new HashMap<>();
        hourlyPattern.forEach((hour, count) -> {
            int ridersNeeded = (int) Math.ceil(count / 3.0); // 3 deliveries per rider per hour
            schedule.put(hour, ridersNeeded);
        });
        return schedule;
    }

    private String findPeakTime(Map<Integer, Long> hourlyPattern) {
        return hourlyPattern.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(e -> String.format("%02d:00", e.getKey()))
                .orElse("12:00");
    }

    private String calculateSystemLoad(int pendingDeliveries, int availableRiders) {
        if (availableRiders == 0) return "CRITICAL";
        double load = (double) pendingDeliveries / availableRiders;
        if (load < 2) return "LOW";
        if (load < 4) return "NORMAL";
        if (load < 6) return "HIGH";
        return "CRITICAL";
    }

    private List<String> generateRecommendedActions() {
        List<String> actions = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();
        
        if (hour >= 11 && hour <= 13) {
            actions.add("점심 피크 시간: 추가 라이더 대기 권장");
        }
        if (hour >= 18 && hour <= 20) {
            actions.add("저녁 피크 시간: 최대 라이더 운용 권장");
        }
        
        actions.add("실시간 모니터링을 통한 동적 라이더 배치");
        actions.add("AI 기반 수요 예측 시스템 활용");
        
        return actions;
    }
}
