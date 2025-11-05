package com.delivery.quickdeliver.repository;

import com.delivery.quickdeliver.domain.entity.Rider;
import com.delivery.quickdeliver.domain.enums.RiderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RiderRepository extends JpaRepository<Rider, Long> {
    
    Optional<Rider> findByRiderId(String riderId);
    
    Optional<Rider> findByEmail(String email);
    
    Optional<Rider> findByPhoneNumber(String phoneNumber);
    
    List<Rider> findByStatus(RiderStatus status);
    
    // 특정 위치 근처의 가용 라이더 찾기 (거리 기반)
    @Query(value = "SELECT * FROM riders r WHERE r.status = 'AVAILABLE' " +
            "AND ST_Distance_Sphere(point(r.current_longitude, r.current_latitude), " +
            "point(:longitude, :latitude)) <= :radius " +
            "ORDER BY ST_Distance_Sphere(point(r.current_longitude, r.current_latitude), " +
            "point(:longitude, :latitude))", nativeQuery = true)
    List<Rider> findAvailableRidersNearby(@Param("latitude") Double latitude,
                                          @Param("longitude") Double longitude,
                                          @Param("radius") Double radius);
    
    // 간단한 거리 계산 버전 (H2 DB용)
    @Query("SELECT r FROM Rider r WHERE r.status = 'AVAILABLE' " +
            "AND SQRT(POWER(r.currentLatitude - :latitude, 2) + " +
            "POWER(r.currentLongitude - :longitude, 2)) * 111 <= :radiusKm " +
            "ORDER BY SQRT(POWER(r.currentLatitude - :latitude, 2) + " +
            "POWER(r.currentLongitude - :longitude, 2))")
    List<Rider> findAvailableRidersWithinRadius(@Param("latitude") Double latitude,
                                                @Param("longitude") Double longitude,
                                                @Param("radiusKm") Double radiusKm);
    
    @Query("SELECT r FROM Rider r WHERE r.shiftStartTime IS NOT NULL " +
            "AND r.shiftEndTime IS NULL")
    List<Rider> findActiveRiders();
}
