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
                    .status(RiderStatus.BUSY)  // 배송 중 상태로 시작
                    .vehicleType(VehicleType.MOTORCYCLE)
                    .vehicleNumber("12가3456")
                    .currentLatitude(37.5012)  // 픽업 지점에서 시작
                    .currentLongitude(127.0396)
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
                    .status(RiderStatus.BUSY)  // 배송 중 상태로 시작
                    .vehicleType(VehicleType.BIKE)
                    .vehicleNumber("34나5678")
                    .currentLatitude(37.5636)  // 명동 픽업 지점에서 시작
                    .currentLongitude(126.9826)
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
                    .rider(rider1)
                    .status(DeliveryStatus.IN_TRANSIT)
                    .priority(Priority.NORMAL)
                    .itemDescription("치킨 2마리 + 콜라 2병")
                    .weight(3.5)
                    .quantity(1)
                    .deliveryFee(3500)
                    .requestedTime(LocalDateTime.now().minusMinutes(30))
                    .estimatedPickupTime(LocalDateTime.now().minusMinutes(20))
                    .estimatedDeliveryTime(LocalDateTime.now().plusMinutes(10))
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

            Delivery delivery3 = Delivery.builder()
                    .deliveryId("DEL-TEST003")
                    .orderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8))
                    .pickupAddress(Address.builder()
                            .address("서울특별시 송파구 올림픽로 240")
                            .detailAddress("롯데월드타워 1층")
                            .latitude(37.5125)
                            .longitude(127.1025)
                            .contactName("송파상점")
                            .contactPhone("02-3456-7890")
                            .build())
                    .deliveryAddress(Address.builder()
                            .address("서울특별시 강남구 봉은사로 524")
                            .detailAddress("삼성동 아파트 205동 501호")
                            .latitude(37.5140)
                            .longitude(127.0594)
                            .contactName("고객C")
                            .contactPhone("010-7654-3210")
                            .build())
                    .status(DeliveryStatus.PENDING)
                    .priority(Priority.URGENT)
                    .itemDescription("의료용품 긴급배송")
                    .weight(1.0)
                    .quantity(1)
                    .deliveryFee(8000)
                    .requestedTime(LocalDateTime.now())
                    .estimatedDeliveryTime(LocalDateTime.now().plusMinutes(20))
                    .estimatedDistance(1.8)
                    .build();

            Delivery delivery4 = Delivery.builder()
                    .deliveryId("DEL-TEST004")
                    .orderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8))
                    .pickupAddress(Address.builder()
                            .address("서울특별시 중구 명동길 14")
                            .detailAddress("명동상가 2층")
                            .latitude(37.5636)
                            .longitude(126.9826)
                            .contactName("명동상점")
                            .contactPhone("02-4567-8901")
                            .build())
                    .deliveryAddress(Address.builder()
                            .address("서울특별시 용산구 한강대로 405")
                            .detailAddress("용산역 3번 출구")
                            .latitude(37.5295)
                            .longitude(126.9645)
                            .contactName("고객D")
                            .contactPhone("010-6543-2109")
                            .build())
                    .rider(rider2)
                    .status(DeliveryStatus.PICKING_UP)
                    .priority(Priority.NORMAL)
                    .itemDescription("서류 봉투")
                    .weight(0.5)
                    .quantity(1)
                    .deliveryFee(4000)
                    .requestedTime(LocalDateTime.now().minusMinutes(40))
                    .estimatedPickupTime(LocalDateTime.now().minusMinutes(35))
                    .estimatedDeliveryTime(LocalDateTime.now().plusMinutes(5))
                    .estimatedDistance(4.2)
                    .build();

            Delivery delivery5 = Delivery.builder()
                    .deliveryId("DEL-TEST005")
                    .orderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8))
                    .pickupAddress(Address.builder()
                            .address("서울특별시 마포구 월드컵로 240")
                            .detailAddress("홍대입구역 근처 음식점")
                            .latitude(37.5567)
                            .longitude(126.9225)
                            .contactName("홍대맛집")
                            .contactPhone("02-5678-9012")
                            .build())
                    .deliveryAddress(Address.builder()
                            .address("서울특별시 마포구 서강로 100")
                            .detailAddress("서강대학교 기숙사")
                            .latitude(37.5508)
                            .longitude(126.9406)
                            .contactName("고객E")
                            .contactPhone("010-5432-1098")
                            .build())
                    .rider(rider1)
                    .status(DeliveryStatus.DELIVERED)
                    .priority(Priority.NORMAL)
                    .itemDescription("피자 2판 + 사이다")
                    .weight(4.0)
                    .quantity(1)
                    .deliveryFee(4500)
                    .requestedTime(LocalDateTime.now().minusHours(1))
                    .estimatedPickupTime(LocalDateTime.now().minusMinutes(50))
                    .estimatedDeliveryTime(LocalDateTime.now().minusMinutes(30))
                    .actualDistance(2.1)
                    .estimatedDistance(2.0)
                    .build();

            Delivery delivery6 = Delivery.builder()
                    .deliveryId("DEL-TEST006")
                    .orderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8))
                    .pickupAddress(Address.builder()
                            .address("서울특별시 종로구 종로 51")
                            .detailAddress("종각역 백화점")
                            .latitude(37.5695)
                            .longitude(126.9826)
                            .contactName("종로상점")
                            .contactPhone("02-6789-0123")
                            .build())
                    .deliveryAddress(Address.builder()
                            .address("서울특별시 성북구 동소문로 123")
                            .detailAddress("성북구청 근처")
                            .latitude(37.5894)
                            .longitude(127.0167)
                            .contactName("고객F")
                            .contactPhone("010-4321-0987")
                            .build())
                    .status(DeliveryStatus.PENDING)
                    .priority(Priority.LOW)
                    .itemDescription("책 5권")
                    .weight(2.5)
                    .quantity(1)
                    .deliveryFee(3000)
                    .requestedTime(LocalDateTime.now().plusMinutes(30))
                    .estimatedDeliveryTime(LocalDateTime.now().plusHours(2))
                    .estimatedDistance(3.5)
                    .build();

            deliveryRepository.save(delivery1);
            deliveryRepository.save(delivery2);
            deliveryRepository.save(delivery3);
            deliveryRepository.save(delivery4);
            deliveryRepository.save(delivery5);
            deliveryRepository.save(delivery6);

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
            log.info("Test Deliveries:");
            log.info("DEL-TEST001: IN_TRANSIT (rider1 - 김배달, BUSY) - 위치 시뮬레이션 자동 시작");
            log.info("DEL-TEST002: PENDING (긴급 - 노트북)");
            log.info("DEL-TEST003: PENDING (초긴급 - 의료용품)");
            log.info("DEL-TEST004: PICKING_UP (rider2 - 이퀵, BUSY) - 위치 시뮬레이션 자동 시작");
            log.info("DEL-TEST005: DELIVERED (rider1 - 완료됨)");
            log.info("DEL-TEST006: PENDING (예약 - 책)");
            log.info("=".repeat(50));
        };
    }
}
