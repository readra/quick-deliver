package com.delivery.quickdeliver.service;

import com.delivery.quickdeliver.domain.entity.Delivery;
import com.delivery.quickdeliver.domain.entity.DeliveryHistory;
import com.delivery.quickdeliver.domain.entity.Rider;
import com.delivery.quickdeliver.domain.enums.DeliveryStatus;
import com.delivery.quickdeliver.domain.enums.Priority;
import com.delivery.quickdeliver.domain.enums.RiderStatus;
import com.delivery.quickdeliver.dto.request.DeliveryCreateRequest;
import com.delivery.quickdeliver.dto.request.DeliveryStatusUpdateRequest;
import com.delivery.quickdeliver.dto.response.DeliveryResponse;
import com.delivery.quickdeliver.dto.response.DeliveryTrackingResponse;
import com.delivery.quickdeliver.exception.ResourceNotFoundException;
import com.delivery.quickdeliver.repository.DeliveryRepository;
import com.delivery.quickdeliver.repository.RiderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final RiderRepository riderRepository;
    private final RouteOptimizationService routeOptimizationService;
    private final NotificationService notificationService;

    @Transactional
    public DeliveryResponse createDelivery(DeliveryCreateRequest request) {
        log.info("Creating new delivery from {} to {}", 
                request.getPickupAddress().getAddress(), 
                request.getDeliveryAddress().getAddress());

        Delivery delivery = Delivery.builder()
                .deliveryId(generateDeliveryId())
                .orderNumber(request.getOrderNumber())
                .pickupAddress(request.getPickupAddress())
                .deliveryAddress(request.getDeliveryAddress())
                .status(DeliveryStatus.PENDING)
                .priority(request.getPriority())
                .itemDescription(request.getItemDescription())
                .weight(request.getWeight())
                .quantity(request.getQuantity())
                .deliveryFee(request.getDeliveryFee())
                .requestedTime(LocalDateTime.now())
                .estimatedDeliveryTime(calculateEstimatedDeliveryTime(request.getPriority()))
                .specialInstructions(request.getSpecialInstructions())
                .build();

        // 거리 계산 및 최적 경로 설정
        Double distance = routeOptimizationService.calculateDistance(
                request.getPickupAddress(), request.getDeliveryAddress());
        delivery.setEstimatedDistance(distance);

        delivery = deliveryRepository.save(delivery);
        
        // 자동 배정 시도
        assignOptimalRider(delivery);
        
        return DeliveryResponse.from(delivery);
    }

    @Transactional
    public void assignOptimalRider(Delivery delivery) {
        log.info("Finding optimal rider for delivery {}", delivery.getDeliveryId());

        // 픽업 위치 근처의 가용 라이더 찾기
        List<Rider> availableRiders = riderRepository.findAvailableRidersWithinRadius(
                delivery.getPickupAddress().getLatitude(),
                delivery.getPickupAddress().getLongitude(),
                5.0 // 5km 반경
        );

        if (!availableRiders.isEmpty()) {
            // 최적 라이더 선택 (거리, 성과, 차량 타입 고려)
            Rider optimalRider = selectOptimalRider(availableRiders, delivery);
            
            delivery.assignRider(optimalRider);
            optimalRider.setStatus(RiderStatus.BUSY);
            
            deliveryRepository.save(delivery);
            riderRepository.save(optimalRider);
            
            // 라이더에게 알림
            notificationService.notifyRiderAssignment(optimalRider, delivery);
            
            log.info("Assigned delivery {} to rider {}", 
                    delivery.getDeliveryId(), optimalRider.getRiderId());
        } else {
            log.warn("No available riders found for delivery {}", delivery.getDeliveryId());
        }
    }

    private Rider selectOptimalRider(List<Rider> riders, Delivery delivery) {
        return riders.stream()
                .filter(r -> r.getVehicleType().getMaxWeight() >= delivery.getWeight())
                .sorted((r1, r2) -> {
                    // 거리 기반 정렬 (가장 가까운 라이더)
                    double dist1 = calculateDistance(
                            r1.getCurrentLatitude(), r1.getCurrentLongitude(),
                            delivery.getPickupAddress().getLatitude(),
                            delivery.getPickupAddress().getLongitude());
                    double dist2 = calculateDistance(
                            r2.getCurrentLatitude(), r2.getCurrentLongitude(),
                            delivery.getPickupAddress().getLatitude(),
                            delivery.getPickupAddress().getLongitude());
                    return Double.compare(dist1, dist2);
                })
                .findFirst()
                .orElse(riders.get(0));
    }

    @Transactional
    public DeliveryResponse updateDeliveryStatus(String deliveryId, 
                                                 DeliveryStatusUpdateRequest request) {
        Delivery delivery = deliveryRepository.findByDeliveryId(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found"));

        DeliveryStatus newStatus = request.getStatus();
        
        switch (newStatus) {
            case PICKING_UP:
                delivery.startPickup();
                break;
            case IN_TRANSIT:
                delivery.completePickup();
                break;
            case DELIVERED:
                delivery.completeDelivery();
                if (delivery.getRider() != null) {
                    delivery.getRider().setStatus(RiderStatus.AVAILABLE);
                    delivery.getRider().setTotalDeliveries(
                            delivery.getRider().getTotalDeliveries() + 1);
                }
                break;
            case CANCELLED:
                delivery.cancelDelivery(request.getReason());
                if (delivery.getRider() != null) {
                    delivery.getRider().setStatus(RiderStatus.AVAILABLE);
                }
                break;
            default:
                delivery.setStatus(newStatus);
        }

        // 위치 정보가 있으면 히스토리에 추가
        if (request.getLatitude() != null && request.getLongitude() != null) {
            DeliveryHistory history = DeliveryHistory.builder()
                    .delivery(delivery)
                    .event("Status updated to " + newStatus)
                    .status(newStatus)
                    .latitude(request.getLatitude())
                    .longitude(request.getLongitude())
                    .build();
            delivery.getHistories().add(history);
        }

        delivery = deliveryRepository.save(delivery);
        
        // 고객에게 알림
        notificationService.notifyStatusChange(delivery);
        
        return DeliveryResponse.from(delivery);
    }

    public DeliveryTrackingResponse trackDelivery(String deliveryId) {
        Delivery delivery = deliveryRepository.findByDeliveryId(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found"));

        return DeliveryTrackingResponse.builder()
                .deliveryId(delivery.getDeliveryId())
                .currentStatus(delivery.getStatus())
                .currentLocation(delivery.getRider() != null ? 
                        new DeliveryTrackingResponse.Location(
                                delivery.getRider().getCurrentLatitude(),
                                delivery.getRider().getCurrentLongitude()
                        ) : null)
                .estimatedDeliveryTime(delivery.getEstimatedDeliveryTime())
                .histories(delivery.getHistories().stream()
                        .map(h -> DeliveryTrackingResponse.History.builder()
                                .event(h.getEvent())
                                .status(h.getStatus())
                                .eventTime(h.getEventTime())
                                .location(h.getLatitude() != null ? 
                                        new DeliveryTrackingResponse.Location(
                                                h.getLatitude(), h.getLongitude()
                                        ) : null)
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    public List<DeliveryResponse> getPendingDeliveries() {
        return deliveryRepository.findPendingDeliveries().stream()
                .map(DeliveryResponse::from)
                .collect(Collectors.toList());
    }

    public List<DeliveryResponse> getRiderDeliveries(String riderId) {
        Rider rider = riderRepository.findByRiderId(riderId)
                .orElseThrow(() -> new ResourceNotFoundException("Rider not found"));
        
        return deliveryRepository.findByRider(rider).stream()
                .map(DeliveryResponse::from)
                .collect(Collectors.toList());
    }

    private String generateDeliveryId() {
        return "DEL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private LocalDateTime calculateEstimatedDeliveryTime(Priority priority) {
        return LocalDateTime.now().plusMinutes(priority.getMaxMinutes());
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Haversine formula for distance calculation
        double R = 6371; // Earth's radius in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
