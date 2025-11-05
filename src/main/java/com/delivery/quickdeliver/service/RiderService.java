package com.delivery.quickdeliver.service;

import com.delivery.quickdeliver.domain.entity.Rider;
import com.delivery.quickdeliver.domain.entity.RiderPerformance;
import com.delivery.quickdeliver.domain.enums.RiderStatus;
import com.delivery.quickdeliver.dto.request.LocationUpdateRequest;
import com.delivery.quickdeliver.dto.request.RiderRegisterRequest;
import com.delivery.quickdeliver.dto.response.RiderDashboardResponse;
import com.delivery.quickdeliver.dto.response.RiderResponse;
import com.delivery.quickdeliver.exception.DuplicateResourceException;
import com.delivery.quickdeliver.exception.ResourceNotFoundException;
import com.delivery.quickdeliver.repository.DeliveryRepository;
import com.delivery.quickdeliver.repository.RiderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RiderService {

    private final RiderRepository riderRepository;
    private final DeliveryRepository deliveryRepository;
    private final WebSocketService webSocketService;

    @Transactional
    public RiderResponse registerRider(RiderRegisterRequest request) {
        log.info("Registering new rider: {}", request.getEmail());

        // 중복 체크
        if (riderRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Email already exists");
        }
        if (riderRepository.findByPhoneNumber(request.getPhoneNumber()).isPresent()) {
            throw new DuplicateResourceException("Phone number already exists");
        }

        Rider rider = Rider.builder()
                .riderId(generateRiderId())
                .name(request.getName())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .status(RiderStatus.OFFLINE)
                .vehicleType(request.getVehicleType())
                .vehicleNumber(request.getVehicleNumber())
                .totalDeliveries(0)
                .averageRating(5.0)
                .totalDistance(0.0)
                .build();

        rider = riderRepository.save(rider);
        return RiderResponse.from(rider);
    }

    @Transactional
    public void updateLocation(String riderId, LocationUpdateRequest request) {
        Rider rider = riderRepository.findByRiderId(riderId)
                .orElseThrow(() -> new ResourceNotFoundException("Rider not found"));

        rider.updateLocation(request.getLatitude(), request.getLongitude());
        riderRepository.save(rider);

        // WebSocket을 통해 실시간 위치 브로드캐스트
        webSocketService.broadcastRiderLocation(riderId, request.getLatitude(), request.getLongitude());
        
        log.debug("Updated location for rider {}: {}, {}", 
                riderId, request.getLatitude(), request.getLongitude());
    }

    @Transactional
    public void startShift(String riderId) {
        Rider rider = riderRepository.findByRiderId(riderId)
                .orElseThrow(() -> new ResourceNotFoundException("Rider not found"));

        rider.startShift();
        riderRepository.save(rider);
        
        log.info("Rider {} started shift", riderId);
    }

    @Transactional
    public void endShift(String riderId) {
        Rider rider = riderRepository.findByRiderId(riderId)
                .orElseThrow(() -> new ResourceNotFoundException("Rider not found"));

        rider.endShift();
        riderRepository.save(rider);

        // 일일 성과 계산 및 저장
        calculateDailyPerformance(rider);
        
        log.info("Rider {} ended shift", riderId);
    }

    @Transactional
    public void updateStatus(String riderId, RiderStatus status) {
        Rider rider = riderRepository.findByRiderId(riderId)
                .orElseThrow(() -> new ResourceNotFoundException("Rider not found"));

        rider.setStatus(status);
        riderRepository.save(rider);
        
        log.info("Rider {} status updated to {}", riderId, status);
    }

    public RiderDashboardResponse getRiderDashboard(String riderId) {
        Rider rider = riderRepository.findByRiderId(riderId)
                .orElseThrow(() -> new ResourceNotFoundException("Rider not found"));

        // 오늘의 배송 건수
        Long todayDeliveries = deliveryRepository.countDailyDeliveriesByRider(
                rider, LocalDateTime.now());

        // 평균 배송 시간
        Double avgDeliveryTime = deliveryRepository.getAverageDeliveryTimeByRider(rider);

        return RiderDashboardResponse.builder()
                .riderId(rider.getRiderId())
                .name(rider.getName())
                .status(rider.getStatus())
                .todayDeliveries(todayDeliveries.intValue())
                .totalDeliveries(rider.getTotalDeliveries())
                .averageRating(rider.getAverageRating())
                .averageDeliveryTime(avgDeliveryTime != null ? avgDeliveryTime : 0.0)
                .currentLocation(rider.getCurrentLatitude() != null ? 
                        new RiderDashboardResponse.Location(
                                rider.getCurrentLatitude(), 
                                rider.getCurrentLongitude()
                        ) : null)
                .shiftStartTime(rider.getShiftStartTime())
                .build();
    }

    public List<RiderResponse> getAvailableRiders() {
        return riderRepository.findByStatus(RiderStatus.AVAILABLE).stream()
                .map(RiderResponse::from)
                .collect(Collectors.toList());
    }

    public List<RiderResponse> getActiveRiders() {
        return riderRepository.findActiveRiders().stream()
                .map(RiderResponse::from)
                .collect(Collectors.toList());
    }

    private void calculateDailyPerformance(Rider rider) {
        LocalDate today = LocalDate.now();
        
        // 오늘의 성과 데이터 수집
        Long deliveryCount = deliveryRepository.countDailyDeliveriesByRider(
                rider, LocalDateTime.now());
        Double avgDeliveryTime = deliveryRepository.getAverageDeliveryTimeByRider(rider);

        RiderPerformance performance = RiderPerformance.builder()
                .rider(rider)
                .date(today)
                .totalDeliveries(deliveryCount.intValue())
                .averageDeliveryTime(avgDeliveryTime != null ? avgDeliveryTime : 0.0)
                .averageRating(rider.getAverageRating())
                .build();

        performance.calculateEfficiencyScore();
        rider.getPerformances().add(performance);
    }

    private String generateRiderId() {
        return "RDR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
