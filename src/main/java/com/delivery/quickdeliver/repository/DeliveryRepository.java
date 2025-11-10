package com.delivery.quickdeliver.repository;

import com.delivery.quickdeliver.domain.entity.Delivery;
import com.delivery.quickdeliver.domain.entity.Rider;
import com.delivery.quickdeliver.domain.enums.DeliveryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    
    Optional<Delivery> findByDeliveryId(String deliveryId);
    
    Optional<Delivery> findByOrderNumber(String orderNumber);
    
    List<Delivery> findByStatus(DeliveryStatus status);
    
    List<Delivery> findByRider(Rider rider);
    
    Page<Delivery> findByRiderAndStatus(Rider rider, DeliveryStatus status, Pageable pageable);
    
    // 대기 중인 배송 찾기 (우선순위 순)
    @Query("SELECT d FROM Delivery d WHERE d.status = 'PENDING' " +
            "ORDER BY d.priority DESC, d.requestedTime ASC")
    List<Delivery> findPendingDeliveries();
    
    // 특정 기간의 배송 조회
    @Query("SELECT d FROM Delivery d WHERE d.requestedTime BETWEEN :startDate AND :endDate")
    List<Delivery> findDeliveriesBetweenDates(@Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);
    
    // 지연된 배송 찾기
    @Query("SELECT d FROM Delivery d WHERE d.status IN ('ASSIGNED', 'PICKING_UP', 'IN_TRANSIT') " +
            "AND d.estimatedDeliveryTime < :now")
    List<Delivery> findDelayedDeliveries(@Param("now") LocalDateTime now);
    
    // 라이더별 일일 배송 건수 (Native Query)
    @Query(value = "SELECT COUNT(*) FROM deliveries d WHERE d.rider_id = :#{#rider.id} " +
            "AND CAST(d.actual_delivery_time AS DATE) = CAST(:date AS DATE) " +
            "AND d.status = 'DELIVERED'", 
            nativeQuery = true)
    Long countDailyDeliveriesByRider(@Param("rider") Rider rider, 
                                     @Param("date") LocalDateTime date);
    
    // 배송 성과 통계 (H2/PostgreSQL 호환)
    @Query(value = "SELECT AVG(EXTRACT(EPOCH FROM (d.actual_delivery_time - d.requested_time)) / 60) " +
            "FROM deliveries d WHERE d.rider_id = :#{#rider.id} AND d.status = 'DELIVERED'", 
            nativeQuery = true)
    Double getAverageDeliveryTimeByRider(@Param("rider") Rider rider);
}
