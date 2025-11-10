package com.delivery.quickdeliver.service;

import com.delivery.quickdeliver.domain.entity.Delivery;
import com.delivery.quickdeliver.domain.entity.Rider;
import com.delivery.quickdeliver.domain.enums.DeliveryStatus;
import com.delivery.quickdeliver.domain.enums.RiderStatus;
import com.delivery.quickdeliver.dto.response.DeliveryResponse;
import com.delivery.quickdeliver.dto.response.RiderResponse;
import com.delivery.quickdeliver.exception.InvalidRequestException;
import com.delivery.quickdeliver.exception.ResourceNotFoundException;
import com.delivery.quickdeliver.repository.DeliveryRepository;
import com.delivery.quickdeliver.repository.RiderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BackOfficeService {

    private final RiderRepository riderRepository;
    private final DeliveryRepository deliveryRepository;
    private final DeliveryService deliveryService;
    private final NotificationService notificationService;

    /**
     * 전체 대시보드 정보 조회
     */
    public Map<String, Object> getDashboard() {
        Map<String, Object> dashboard = new HashMap<>();
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = now.toLocalDate().atStartOfDay();
        
        // 오늘의 배송 통계
        List<Delivery> todayDeliveries = deliveryRepository.findDeliveriesBetweenDates(
                todayStart, now);
        
        long totalDeliveries = todayDeliveries.size();
        long completedDeliveries = todayDeliveries.stream()
                .filter(d -> d.getStatus() == DeliveryStatus.DELIVERED)
                .count();
        long inProgressDeliveries = todayDeliveries.stream()
                .filter(d -> d.getStatus() == DeliveryStatus.IN_TRANSIT || 
                           d.getStatus() == DeliveryStatus.PICKING_UP)
                .count();
        long pendingDeliveries = todayDeliveries.stream()
                .filter(d -> d.getStatus() == DeliveryStatus.PENDING)
                .count();
        
        dashboard.put("totalDeliveriesToday", totalDeliveries);
        dashboard.put("completedDeliveries", completedDeliveries);
        dashboard.put("inProgressDeliveries", inProgressDeliveries);
        dashboard.put("pendingDeliveries", pendingDeliveries);
        dashboard.put("completionRate", 
                totalDeliveries > 0 ? (double) completedDeliveries / totalDeliveries * 100 : 0);
        
        // 라이더 현황
        List<Rider> allRiders = riderRepository.findAll();
        long totalRiders = allRiders.size();
        long activeRiders = riderRepository.findActiveRiders().size();
        long availableRiders = allRiders.stream()
                .filter(r -> r.getStatus() == RiderStatus.AVAILABLE)
                .count();
        long busyRiders = allRiders.stream()
                .filter(r -> r.getStatus() == RiderStatus.BUSY)
                .count();
        
        dashboard.put("totalRiders", totalRiders);
        dashboard.put("activeRiders", activeRiders);
        dashboard.put("availableRiders", availableRiders);
        dashboard.put("busyRiders", busyRiders);
        
        // 평균 배송 시간
        double avgDeliveryTime = todayDeliveries.stream()
                .filter(d -> d.getActualDeliveryTime() != null && d.getRequestedTime() != null)
                .mapToLong(d -> ChronoUnit.MINUTES.between(
                        d.getRequestedTime(), d.getActualDeliveryTime()))
                .average()
                .orElse(0.0);
        dashboard.put("avgDeliveryTimeMinutes", avgDeliveryTime);
        
        // 지연 배송
        List<Delivery> delayedDeliveries = deliveryRepository.findDelayedDeliveries(now);
        dashboard.put("delayedDeliveries", delayedDeliveries.size());
        
        // 시간대별 배송 건수 (오늘)
        Map<Integer, Long> hourlyDistribution = todayDeliveries.stream()
                .collect(Collectors.groupingBy(
                        d -> d.getRequestedTime().getHour(),
                        Collectors.counting()
                ));
        dashboard.put("hourlyDistribution", hourlyDistribution);
        
        return dashboard;
    }

    /**
     * 전체 라이더 목록 조회
     */
    public List<RiderResponse> getAllRiders(RiderStatus status) {
        List<Rider> riders;
        
        if (status != null) {
            riders = riderRepository.findByStatus(status);
        } else {
            riders = riderRepository.findAll();
        }
        
        return riders.stream()
                .map(RiderResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 라이더 상세 정보 조회
     */
    public Map<String, Object> getRiderDetail(String riderId) {
        Rider rider = riderRepository.findByRiderId(riderId)
                .orElseThrow(() -> new ResourceNotFoundException("Rider not found"));
        
        Map<String, Object> detail = new HashMap<>();
        detail.put("rider", RiderResponse.from(rider));
        
        // 최근 배송 이력
        List<Delivery> recentDeliveries = deliveryRepository.findByRider(rider).stream()
                .sorted(Comparator.comparing(Delivery::getRequestedTime).reversed())
                .limit(10)
                .toList();
        detail.put("recentDeliveries", recentDeliveries.stream()
                .map(DeliveryResponse::from)
                .collect(Collectors.toList()));
        
        // 오늘의 성과
        LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
        long todayDeliveries = deliveryRepository.countDailyDeliveriesByRider(
                rider, LocalDateTime.now());
        detail.put("todayDeliveries", todayDeliveries);
        
        // 주간 성과
        LocalDateTime weekStart = LocalDateTime.now().minusDays(7);
        List<Delivery> weekDeliveries = deliveryRepository.findDeliveriesBetweenDates(
                weekStart, LocalDateTime.now()).stream()
                .filter(d -> d.getRider() != null && d.getRider().equals(rider))
                .toList();
        detail.put("weekDeliveries", weekDeliveries.size());
        
        return detail;
    }

    /**
     * 라이더 활성화
     */
    @Transactional
    public void activateRider(String riderId) {
        Rider rider = riderRepository.findByRiderId(riderId)
                .orElseThrow(() -> new ResourceNotFoundException("Rider not found"));
        
        // 실제로는 isActive 같은 필드가 있어야 하지만, 
        // 현재는 status를 OFFLINE으로 변경
        rider.setStatus(RiderStatus.OFFLINE);
        riderRepository.save(rider);
        
        log.info("Rider {} activated", riderId);
    }

    /**
     * 라이더 비활성화
     */
    @Transactional
    public void deactivateRider(String riderId) {
        Rider rider = riderRepository.findByRiderId(riderId)
                .orElseThrow(() -> new ResourceNotFoundException("Rider not found"));
        
        // 진행 중인 배송이 있는지 확인
        List<Delivery> activeDeliveries = deliveryRepository.findByRider(rider).stream()
                .filter(d -> d.getStatus() == DeliveryStatus.ASSIGNED ||
                           d.getStatus() == DeliveryStatus.PICKING_UP ||
                           d.getStatus() == DeliveryStatus.IN_TRANSIT)
                .toList();
        
        if (!activeDeliveries.isEmpty()) {
            throw new InvalidRequestException(
                    "진행 중인 배송이 있어 비활성화할 수 없습니다. 배송 수: " + activeDeliveries.size());
        }
        
        rider.setStatus(RiderStatus.OFFLINE);
        riderRepository.save(rider);
        
        log.info("Rider {} deactivated", riderId);
    }

    /**
     * 라이더 위치 지도 정보
     */
    public List<Map<String, Object>> getRidersLocationMap() {
        List<Rider> activeRiders = riderRepository.findActiveRiders();
        
        return activeRiders.stream()
                .filter(r -> r.getCurrentLatitude() != null && r.getCurrentLongitude() != null)
                .map(rider -> {
                    Map<String, Object> location = new HashMap<>();
                    location.put("riderId", rider.getRiderId());
                    location.put("name", rider.getName());
                    location.put("status", rider.getStatus());
                    location.put("vehicleType", rider.getVehicleType());
                    location.put("latitude", rider.getCurrentLatitude());
                    location.put("longitude", rider.getCurrentLongitude());
                    location.put("lastUpdate", rider.getLastLocationUpdate());
                    
                    // 현재 배송 정보
                    List<Delivery> currentDeliveries = deliveryRepository.findByRider(rider).stream()
                            .filter(d -> d.getStatus() == DeliveryStatus.ASSIGNED ||
                                       d.getStatus() == DeliveryStatus.PICKING_UP ||
                                       d.getStatus() == DeliveryStatus.IN_TRANSIT)
                            .toList();
                    location.put("currentDeliveries", currentDeliveries.size());
                    
                    return location;
                })
                .collect(Collectors.toList());
    }

    /**
     * 전체 배송 목록 조회
     */
    public List<DeliveryResponse> getAllDeliveries(DeliveryStatus status, 
                                                   LocalDateTime startDate, 
                                                   LocalDateTime endDate) {
        List<Delivery> deliveries;
        
        if (startDate != null && endDate != null) {
            deliveries = deliveryRepository.findDeliveriesBetweenDates(startDate, endDate);
        } else {
            deliveries = deliveryRepository.findAll();
        }
        
        if (status != null) {
            deliveries = deliveries.stream()
                    .filter(d -> d.getStatus() == status)
                    .collect(Collectors.toList());
        }
        
        return deliveries.stream()
                .map(DeliveryResponse::from)
                .sorted(Comparator.comparing(DeliveryResponse::getRequestedTime).reversed())
                .collect(Collectors.toList());
    }

    /**
     * 배송 상세 정보 조회
     */
    public Map<String, Object> getDeliveryDetail(String deliveryId) {
        Delivery delivery = deliveryRepository.findByDeliveryId(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found"));
        
        Map<String, Object> detail = new HashMap<>();
        detail.put("delivery", DeliveryResponse.from(delivery));
        
        // 배송 이력
        detail.put("histories", delivery.getHistories());
        
        // 라이더 정보
        if (delivery.getRider() != null) {
            detail.put("riderInfo", RiderResponse.from(delivery.getRider()));
        }
        
        // 타임라인 계산
        Map<String, Object> timeline = new HashMap<>();
        if (delivery.getRequestedTime() != null) {
            timeline.put("requested", delivery.getRequestedTime());
        }
        if (delivery.getActualPickupTime() != null) {
            timeline.put("pickedUp", delivery.getActualPickupTime());
            if (delivery.getRequestedTime() != null) {
                long waitTime = ChronoUnit.MINUTES.between(
                        delivery.getRequestedTime(), delivery.getActualPickupTime());
                timeline.put("waitTimeMinutes", waitTime);
            }
        }
        if (delivery.getActualDeliveryTime() != null) {
            timeline.put("delivered", delivery.getActualDeliveryTime());
            if (delivery.getActualPickupTime() != null) {
                long deliveryTime = ChronoUnit.MINUTES.between(
                        delivery.getActualPickupTime(), delivery.getActualDeliveryTime());
                timeline.put("deliveryTimeMinutes", deliveryTime);
            }
        }
        detail.put("timeline", timeline);
        
        return detail;
    }

    /**
     * 수동 배정
     */
    @Transactional
    public DeliveryResponse manualAssignDelivery(String deliveryId, String riderId) {
        Delivery delivery = deliveryRepository.findByDeliveryId(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found"));
        
        Rider rider = riderRepository.findByRiderId(riderId)
                .orElseThrow(() -> new ResourceNotFoundException("Rider not found"));
        
        // 라이더 상태 확인
        if (rider.getStatus() != RiderStatus.AVAILABLE) {
            throw new InvalidRequestException(
                    "라이더가 현재 배송 가능한 상태가 아닙니다. 현재 상태: " + rider.getStatus());
        }
        
        // 배송 상태 확인
        if (delivery.getStatus() != DeliveryStatus.PENDING) {
            throw new InvalidRequestException(
                    "이미 배정된 배송입니다. 현재 상태: " + delivery.getStatus());
        }
        
        // 배정
        delivery.assignRider(rider);
        rider.setStatus(RiderStatus.BUSY);
        
        deliveryRepository.save(delivery);
        riderRepository.save(rider);
        
        // 알림
        notificationService.notifyRiderAssignment(rider, delivery);
        
        log.info("Manually assigned delivery {} to rider {}", deliveryId, riderId);
        
        return DeliveryResponse.from(delivery);
    }

    /**
     * 재배정
     */
    @Transactional
    public DeliveryResponse reassignDelivery(String deliveryId, String newRiderId) {
        Delivery delivery = deliveryRepository.findByDeliveryId(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found"));
        
        // 현재 라이더 해제
        Rider oldRider = delivery.getRider();
        if (oldRider != null) {
            oldRider.setStatus(RiderStatus.AVAILABLE);
            riderRepository.save(oldRider);
        }
        
        delivery.setRider(null);
        delivery.setStatus(DeliveryStatus.PENDING);
        deliveryRepository.save(delivery);
        
        // 새 라이더 배정
        if (newRiderId != null) {
            return manualAssignDelivery(deliveryId, newRiderId);
        } else {
            // 자동 배정
            deliveryService.assignOptimalRider(delivery);
            return DeliveryResponse.from(delivery);
        }
    }

    /**
     * 지연 배송 목록
     */
    public List<DeliveryResponse> getDelayedDeliveries() {
        List<Delivery> delayed = deliveryRepository.findDelayedDeliveries(LocalDateTime.now());
        
        return delayed.stream()
                .map(DeliveryResponse::from)
                .sorted(Comparator.comparing(DeliveryResponse::getEstimatedDeliveryTime))
                .collect(Collectors.toList());
    }

    /**
     * 배송 통계
     */
    public Map<String, Object> getDeliveryStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }
        
        List<Delivery> deliveries = deliveryRepository.findDeliveriesBetweenDates(startDate, endDate);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("period", Map.of("start", startDate, "end", endDate));
        stats.put("totalDeliveries", deliveries.size());
        
        // 상태별 통계
        Map<DeliveryStatus, Long> statusCount = deliveries.stream()
                .collect(Collectors.groupingBy(Delivery::getStatus, Collectors.counting()));
        stats.put("statusDistribution", statusCount);
        
        long completed = statusCount.getOrDefault(DeliveryStatus.DELIVERED, 0L);
        long cancelled = statusCount.getOrDefault(DeliveryStatus.CANCELLED, 0L);
        
        stats.put("completionRate", deliveries.isEmpty() ? 0 : 
                (double) completed / deliveries.size() * 100);
        stats.put("cancellationRate", deliveries.isEmpty() ? 0 : 
                (double) cancelled / deliveries.size() * 100);
        
        // 평균 배송 시간
        double avgTime = deliveries.stream()
                .filter(d -> d.getActualDeliveryTime() != null && d.getRequestedTime() != null)
                .mapToLong(d -> ChronoUnit.MINUTES.between(
                        d.getRequestedTime(), d.getActualDeliveryTime()))
                .average()
                .orElse(0.0);
        stats.put("avgDeliveryTimeMinutes", avgTime);
        
        // 평균 평점
        double avgRating = deliveries.stream()
                .filter(d -> d.getRating() != null)
                .mapToInt(Delivery::getRating)
                .average()
                .orElse(0.0);
        stats.put("avgRating", avgRating);
        
        // 우선순위별 통계
        Map<String, Long> priorityCount = deliveries.stream()
                .collect(Collectors.groupingBy(
                        d -> d.getPriority().name(),
                        Collectors.counting()
                ));
        stats.put("priorityDistribution", priorityCount);
        
        return stats;
    }

    /**
     * 실시간 모니터링
     */
    public Map<String, Object> getRealtimeMonitoring() {
        Map<String, Object> monitoring = new HashMap<>();
        
        // 진행 중인 배송
        List<Delivery> inProgress = deliveryRepository.findAll().stream()
                .filter(d -> d.getStatus() == DeliveryStatus.PICKING_UP ||
                           d.getStatus() == DeliveryStatus.IN_TRANSIT ||
                           d.getStatus() == DeliveryStatus.ASSIGNED)
                .toList();
        
        monitoring.put("activeDeliveries", inProgress.stream()
                .map(DeliveryResponse::from)
                .collect(Collectors.toList()));
        
        // 근무 중인 라이더
        List<Rider> activeRiders = riderRepository.findActiveRiders();
        monitoring.put("activeRidersCount", activeRiders.size());
        
        // 대기 중인 배송
        List<Delivery> pending = deliveryRepository.findPendingDeliveries();
        monitoring.put("pendingDeliveriesCount", pending.size());
        
        // 가용 라이더
        long available = activeRiders.stream()
                .filter(r -> r.getStatus() == RiderStatus.AVAILABLE)
                .count();
        monitoring.put("availableRidersCount", available);
        
        // 시스템 부하
        String loadStatus;
        if (available == 0 && !pending.isEmpty()) {
            loadStatus = "CRITICAL";
        } else if (pending.size() > available * 3) {
            loadStatus = "HIGH";
        } else {
            loadStatus = "NORMAL";
        }
        monitoring.put("systemLoad", loadStatus);
        
        monitoring.put("timestamp", LocalDateTime.now());
        
        return monitoring;
    }

    /**
     * 시스템 알림
     */
    public List<Map<String, Object>> getSystemAlerts() {
        List<Map<String, Object>> alerts = new ArrayList<>();
        
        // 지연 배송 알림
        List<Delivery> delayed = deliveryRepository.findDelayedDeliveries(LocalDateTime.now());
        if (!delayed.isEmpty()) {
            Map<String, Object> alert = new HashMap<>();
            alert.put("type", "DELAYED_DELIVERY");
            alert.put("severity", "HIGH");
            alert.put("message", delayed.size() + "건의 지연 배송이 있습니다.");
            alert.put("count", delayed.size());
            alert.put("timestamp", LocalDateTime.now());
            alerts.add(alert);
        }
        
        // 라이더 부족 알림
        List<Delivery> pending = deliveryRepository.findPendingDeliveries();
        List<Rider> available = riderRepository.findByStatus(RiderStatus.AVAILABLE);
        if (pending.size() > available.size() * 2) {
            Map<String, Object> alert = new HashMap<>();
            alert.put("type", "RIDER_SHORTAGE");
            alert.put("severity", "MEDIUM");
            alert.put("message", "대기 배송 대비 가용 라이더가 부족합니다.");
            alert.put("pendingDeliveries", pending.size());
            alert.put("availableRiders", available.size());
            alert.put("timestamp", LocalDateTime.now());
            alerts.add(alert);
        }
        
        // 오프라인 라이더 알림 (근무 시간 중인데 오프라인)
        List<Rider> activeRiders = riderRepository.findActiveRiders();
        List<Rider> offlineButShifted = activeRiders.stream()
                .filter(r -> r.getStatus() == RiderStatus.OFFLINE && 
                           r.getShiftStartTime() != null &&
                           r.getShiftEndTime() == null)
                .toList();
        
        if (!offlineButShifted.isEmpty()) {
            Map<String, Object> alert = new HashMap<>();
            alert.put("type", "OFFLINE_RIDERS");
            alert.put("severity", "LOW");
            alert.put("message", "근무 중인데 오프라인 상태인 라이더가 있습니다.");
            alert.put("count", offlineButShifted.size());
            alert.put("timestamp", LocalDateTime.now());
            alerts.add(alert);
        }
        
        return alerts;
    }

    /**
     * 라이더 성과 순위
     */
    public List<Map<String, Object>> getRidersPerformanceRanking(
            LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }
        
        List<Rider> riders = riderRepository.findAll();
        LocalDateTime finalStartDate = startDate;
        LocalDateTime finalEndDate = endDate;
        
        return riders.stream()
                .map(rider -> {
                    List<Delivery> riderDeliveries = deliveryRepository.findByRider(rider).stream()
                            .filter(d -> d.getActualDeliveryTime() != null &&
                                       d.getActualDeliveryTime().isAfter(finalStartDate) &&
                                       d.getActualDeliveryTime().isBefore(finalEndDate))
                            .toList();
                    
                    Map<String, Object> performance = new HashMap<>();
                    performance.put("riderId", rider.getRiderId());
                    performance.put("name", rider.getName());
                    performance.put("totalDeliveries", riderDeliveries.size());
                    performance.put("averageRating", rider.getAverageRating());
                    
                    double avgTime = riderDeliveries.stream()
                            .mapToLong(d -> ChronoUnit.MINUTES.between(
                                    d.getRequestedTime(), d.getActualDeliveryTime()))
                            .average()
                            .orElse(0.0);
                    performance.put("avgDeliveryTime", avgTime);
                    
                    // 효율성 점수 계산
                    double score = riderDeliveries.size() * 10 + 
                                 rider.getAverageRating() * 20 - 
                                 avgTime * 0.5;
                    performance.put("efficiencyScore", score);
                    
                    return performance;
                })
                .sorted(Comparator.comparingDouble(
                        (Map<String, Object> m) -> (Double) m.get("efficiencyScore")).reversed())
                .collect(Collectors.toList());
    }

    /**
     * 성과 요약
     */
    public Map<String, Object> getPerformanceSummary(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }
        
        Map<String, Object> summary = new HashMap<>();
        
        // 배송 통계
        Map<String, Object> deliveryStats = getDeliveryStatistics(startDate, endDate);
        summary.put("deliveryStatistics", deliveryStats);
        
        // 라이더 통계
        List<Rider> riders = riderRepository.findAll();
        summary.put("totalRiders", riders.size());
        
        double avgRiderRating = riders.stream()
                .mapToDouble(Rider::getAverageRating)
                .average()
                .orElse(0.0);
        summary.put("avgRiderRating", avgRiderRating);
        
        int totalRiderDeliveries = riders.stream()
                .mapToInt(Rider::getTotalDeliveries)
                .sum();
        summary.put("totalRiderDeliveries", totalRiderDeliveries);
        
        return summary;
    }

    /**
     * 운영 시간 조회
     */
    public Map<String, Object> getOperatingHours() {
        Map<String, Object> hours = new HashMap<>();
        // 실제로는 DB나 설정 파일에서 읽어와야 함
        hours.put("weekday", Map.of("open", "09:00", "close", "22:00"));
        hours.put("weekend", Map.of("open", "10:00", "close", "21:00"));
        return hours;
    }

    /**
     * 운영 시간 업데이트
     */
    @Transactional
    public void updateOperatingHours(Map<String, Object> operatingHours) {
        // 실제로는 DB나 설정 파일에 저장
        log.info("Operating hours updated: {}", operatingHours);
    }

    /**
     * 배송 데이터 CSV 내보내기
     */
    public String exportDeliveriesCSV(LocalDateTime startDate, LocalDateTime endDate) {
        List<Delivery> deliveries = deliveryRepository.findDeliveriesBetweenDates(startDate, endDate);
        
        StringBuilder csv = new StringBuilder();
        csv.append("배송ID,주문번호,상태,우선순위,요청시간,완료시간,라이더ID,라이더명,픽업주소,배송주소,배송료,평점\n");
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        for (Delivery d : deliveries) {
            csv.append(String.format("%s,%s,%s,%s,%s,%s,%s,%s,\"%s\",\"%s\",%d,%s\n",
                    d.getDeliveryId(),
                    d.getOrderNumber(),
                    d.getStatus().getDescription(),
                    d.getPriority().getDescription(),
                    d.getRequestedTime().format(formatter),
                    d.getActualDeliveryTime() != null ? d.getActualDeliveryTime().format(formatter) : "",
                    d.getRider() != null ? d.getRider().getRiderId() : "",
                    d.getRider() != null ? d.getRider().getName() : "",
                    d.getPickupAddress().getAddress(),
                    d.getDeliveryAddress().getAddress(),
                    d.getDeliveryFee(),
                    d.getRating() != null ? d.getRating() : ""
            ));
        }
        
        return csv.toString();
    }
}
