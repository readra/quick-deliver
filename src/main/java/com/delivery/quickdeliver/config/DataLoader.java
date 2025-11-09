package com.delivery.quickdeliver.config;

import com.delivery.quickdeliver.domain.entity.Address;
import com.delivery.quickdeliver.domain.entity.Delivery;
import com.delivery.quickdeliver.domain.entity.Rider;
import com.delivery.quickdeliver.domain.entity.User;
import com.delivery.quickdeliver.domain.enums.DeliveryStatus;
import com.delivery.quickdeliver.domain.enums.Priority;
import com.delivery.quickdeliver.domain.enums.RiderStatus;
import com.delivery.quickdeliver.domain.enums.UserRole;
import com.delivery.quickdeliver.domain.enums.VehicleType;
import com.delivery.quickdeliver.repository.DeliveryRepository;
import com.delivery.quickdeliver.repository.RiderRepository;
import com.delivery.quickdeliver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataLoader {

    @Bean
    @Profile("!test")
    public CommandLineRunner loadData(RiderRepository riderRepository, 
                                     DeliveryRepository deliveryRepository,
                                     UserRepository userRepository,
                                     PasswordEncoder passwordEncoder) {
        return args -> {
            if (riderRepository.count() > 0) {
                log.info("Data already loaded. Skipping...");
                return;
            }

            log.info("Loading initial test data...");

            // 테스트 사용자 생성
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .name("관리자")
                    .email("admin@quickdeliver.com")
                    .phoneNumber("010-0000-0000")
                    .role(UserRole.ADMIN)
                    .enabled(true)
                    .build();

            User backoffice = User.builder()
                    .username("backoffice")
                    .password(passwordEncoder.encode("backoffice123"))
                    .name("백오피스")
                    .email("backoffice@quickdeliver.com")
                    .phoneNumber("010-1111-1111")
                    .role(UserRole.BACKOFFICE)
                    .enabled(true)
                    .build();

            userRepository.save(admin);
            userRepository.save(backoffice);

            // 테스트 라이더 생성
            Rider rider1 = Rider.builder()
                    .riderId("RDR-TEST001")
                    .name("김배달")
                    .phoneNumber("010-1234-5678")
                    .email("rider1@quickdeliver.com")
                    .status(RiderStatus.AVAILABLE)
                    .vehicleType(VehicleType.MOTORCYCLE)
                    .vehicleNumber("12가3456")
                    .currentLatitude(37.5665)
                    .currentLongitude(126.9780)
                    .lastLocationUpdate(LocalDateTime.now())
                    .totalDeliveries(0)
                    .averageRating(5.0)
                    .totalDistance(0.0)
                    .build();

            Rider rider2 = Rider.builder()
                    .riderId("RDR-TEST002")
                    .name("이퀵")
                    .phoneNumber("010-2345-6789")
                    .email("rider2@quickdeliver.com")
                    .status(RiderStatus.AVAILABLE)
                    .vehicleType(VehicleType.BIKE)
                    .vehicleNumber("34나5678")
                    .currentLatitude(37.5700)
                    .currentLongitude(126.9800)
                    .lastLocationUpdate(LocalDateTime.now())
                    .totalDeliveries(0)
                    .averageRating(4.8)
                    .totalDistance(0.0)
                    .build();

            Rider rider3 = Rider.builder()
                    .riderId("RDR-TEST003")
                    .name("박신속")
                    .phoneNumber("010-3456-7890")
                    .email("rider3@quickdeliver.com")
                    .status(RiderStatus.OFFLINE)
                    .vehicleType(VehicleType.CAR)
                    .vehicleNumber("56다7890")
                    .totalDeliveries(0)
                    .averageRating(4.9)
                    .totalDistance(0.0)
                    .build();

            riderRepository.save(rider1);
            riderRepository.save(rider2);
            riderRepository.save(rider3);

            // 라이더 사용자 계정 생성
            User riderUser1 = User.builder()
                    .username("rider1")
                    .password(passwordEncoder.encode("rider123"))
                    .name("김배달")
                    .email("rider1@quickdeliver.com")
                    .phoneNumber("010-1234-5678")
                    .role(UserRole.RIDER)
                    .enabled(true)
                    .rider(rider1)
                    .build();

            User riderUser2 = User.builder()
                    .username("rider2")
                    .password(passwordEncoder.encode("rider123"))
                    .name("이퀵")
                    .email("rider2@quickdeliver.com")
                    .phoneNumber("010-2345-6789")
                    .role(UserRole.RIDER)
                    .enabled(true)
                    .rider(rider2)
                    .build();

            userRepository.save(riderUser1);
            userRepository.save(riderUser2);

            // 테스트 배송 생성
            Delivery delivery1 = Delivery.builder()
                    .deliveryId("DEL-TEST001")
                    .orderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8))
                    .pickupAddress(Address.builder()
                            .address("서울특별시 강남구 테헤란로 123")
                            .detailAddress("ABC빌딩 1층")
                            .latitude(37.5012)
                            .longitude(127.0396)
                            .contactName("김상점")
                            .contactPhone("02-1234-5678")
                            .build())
                    .deliveryAddress(Address.builder()
                            .address("서울특별시 강남구 역삼로 456")
                            .detailAddress("DEF아파트 101동 1001호")
                            .latitude(37.4979)
                            .longitude(127.0276)
                            .contactName("고객A")
                            .contactPhone("010-9876-5432")
                            .build())
                    .status(DeliveryStatus.PENDING)
                    .priority(Priority.NORMAL)
                    .itemDescription("치킨 2마리 + 콜라 2병")
                    .weight(3.5)
                    .quantity(1)
                    .deliveryFee(3500)
                    .requestedTime(LocalDateTime.now())
                    .estimatedDeliveryTime(LocalDateTime.now().plusMinutes(60))
                    .estimatedDistance(2.5)
                    .build();

            Delivery delivery2 = Delivery.builder()
                    .deliveryId("DEL-TEST002")
                    .orderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8))
                    .pickupAddress(Address.builder()
                            .address("서울특별시 서초구 반포대로 789")
                            .detailAddress("GHI타워 5층")
                            .latitude(37.4959)
                            .longitude(127.0051)
                            .contactName("박상점")
                            .contactPhone("02-2345-6789")
                            .build())
                    .deliveryAddress(Address.builder()
                            .address("서울특별시 강남구 선릉로 321")
                            .detailAddress("JKL빌딩 7층")
                            .latitude(37.5048)
                            .longitude(127.0495)
                            .contactName("고객B")
                            .contactPhone("010-8765-4321")
                            .build())
                    .status(DeliveryStatus.PENDING)
                    .priority(Priority.HIGH)
                    .itemDescription("노트북 1대")
                    .weight(2.0)
                    .quantity(1)
                    .deliveryFee(5000)
                    .requestedTime(LocalDateTime.now())
                    .estimatedDeliveryTime(LocalDateTime.now().plusMinutes(30))
                    .estimatedDistance(3.8)
                    .build();

            deliveryRepository.save(delivery1);
            deliveryRepository.save(delivery2);

            log.info("Test data loaded successfully!");
            log.info("Created {} users, {} riders and {} deliveries", 
                    userRepository.count(), riderRepository.count(), deliveryRepository.count());
            log.info("=".repeat(50));
            log.info("Test Accounts:");
            log.info("Admin: username=admin, password=admin123");
            log.info("BackOffice: username=backoffice, password=backoffice123");
            log.info("Rider1: username=rider1, password=rider123");
            log.info("Rider2: username=rider2, password=rider123");
            log.info("=".repeat(50));
        };
    }
}
