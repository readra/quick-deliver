package com.delivery.quickdeliver.service;

import com.delivery.quickdeliver.domain.entity.Delivery;
import com.delivery.quickdeliver.domain.enums.DeliveryStatus;
import com.delivery.quickdeliver.dto.request.DeliveryStatusUpdateRequest;
import com.delivery.quickdeliver.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 지오펜싱 기반 배송 상태 자동 전환 서비스
 *
 * 라이더가 픽업지/배달지 반경 내에 진입하면 배송 상태를 자동으로 전환한다.
 *   ASSIGNED   + 픽업지  100m 이내 진입 → PICKING_UP 자동 전환
 *   PICKING_UP + 배달지  100m 이내 진입 → IN_TRANSIT  자동 전환
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeofencingService {

    /** 자동 상태 전환 임계 거리 (미터) */
    public static final double GEOFENCE_RADIUS_METERS = 100.0;

    private final DeliveryRepository deliveryRepository;
    private final DeliveryService deliveryService;

    /**
     * 라이더의 현재 위치를 기준으로 픽업지·배달지 지오펜스 진입 여부를 확인하고,
     * 조건에 맞으면 배송 상태를 자동으로 전환한다.
     *
     * @param riderId   위치가 업데이트된 라이더 ID
     * @param latitude  현재 위도
     * @param longitude 현재 경도
     */
    public void checkAndAutoTransition(String riderId, double latitude, double longitude) {
        List<Delivery> activeDeliveries = deliveryRepository.findActiveDeliveriesByRiderId(riderId);
        if (activeDeliveries.isEmpty()) return;

        // 가장 최근 진행 중인 배송 하나만 처리
        Delivery delivery = activeDeliveries.get(0);
        DeliveryStatus currentStatus = delivery.getStatus();

        if (currentStatus == DeliveryStatus.ASSIGNED) {
            // 픽업지 진입 여부 확인
            if (hasValidCoordinate(delivery.getPickupAddress())) {
                double dist = distanceMeters(
                        latitude, longitude,
                        delivery.getPickupAddress().getLatitude(),
                        delivery.getPickupAddress().getLongitude()
                );
                if (dist <= GEOFENCE_RADIUS_METERS) {
                    log.info("[지오펜싱] 라이더 {} 픽업지 진입 ({}m) → PICKING_UP 자동 전환",
                            riderId, (int) dist);
                    autoTransition(delivery.getDeliveryId(), DeliveryStatus.PICKING_UP,
                            latitude, longitude);
                }
            }

        } else if (currentStatus == DeliveryStatus.PICKING_UP) {
            // 배달지 진입 여부 확인
            if (hasValidCoordinate(delivery.getDeliveryAddress())) {
                double dist = distanceMeters(
                        latitude, longitude,
                        delivery.getDeliveryAddress().getLatitude(),
                        delivery.getDeliveryAddress().getLongitude()
                );
                if (dist <= GEOFENCE_RADIUS_METERS) {
                    log.info("[지오펜싱] 라이더 {} 배달지 진입 ({}m) → IN_TRANSIT 자동 전환",
                            riderId, (int) dist);
                    autoTransition(delivery.getDeliveryId(), DeliveryStatus.IN_TRANSIT,
                            latitude, longitude);
                }
            }
        }
    }

    // ── 내부 헬퍼 ─────────────────────────────────────────────────────────

    private void autoTransition(String deliveryId, DeliveryStatus newStatus,
                                double latitude, double longitude) {
        DeliveryStatusUpdateRequest req = new DeliveryStatusUpdateRequest();
        req.setStatus(newStatus);
        req.setLatitude(latitude);
        req.setLongitude(longitude);
        deliveryService.updateDeliveryStatus(deliveryId, req);
    }

    private boolean hasValidCoordinate(com.delivery.quickdeliver.domain.entity.Address address) {
        return address != null
                && address.getLatitude() != null
                && address.getLongitude() != null;
    }

    /**
     * Haversine 공식으로 두 좌표 간 거리(미터)를 계산한다.
     */
    public static double distanceMeters(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6_371_000; // 지구 반지름 (m)
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
