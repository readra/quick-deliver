package com.delivery.quickdeliver.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class AnalyticsResponse {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DashboardAnalytics {
        private Integer totalDeliveriesToday;
        private Integer totalDeliveriesWeek;
        private Long completedDeliveries;
        private Long pendingDeliveries;
        private Integer activeRiders;
        private Double averageDeliveryTime;
        private Double onTimeDeliveryRate;
        private Double customerSatisfaction;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EfficiencyAnalytics {
        private Integer totalDeliveries;
        private Double averageDistancePerDelivery;
        private Double routeOptimizationScore;
        private List<String> peakHours;
        private Map<String, Object> deliveryDensityMap;
        private Integer recommendedRiderCount;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RiderPerformanceAnalytics {
        private String riderId;
        private String riderName;
        private Integer totalDeliveries;
        private Double averageDeliveryTime;
        private Double totalDistance;
        private Double averageRating;
        private Double onTimeRate;
        private Double efficiencyScore;
        private List<DailyPerformance> dailyPerformance;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DailyPerformance {
        private LocalDate date;
        private Integer deliveries;
        private Double averageTime;
        private Double efficiencyScore;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PredictiveAnalytics {
        private Long predictedDeliveriesNextHour;
        private Map<Integer, Long> hourlyPattern;
        private Map<String, Long> dailyPattern;
        private Map<Integer, Integer> recommendedRiderSchedule;
        private String expectedPeakTime;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OptimizationSuggestions {
        private List<String> suggestions;
        private String currentLoad;
        private List<String> recommendedActions;
    }
}
