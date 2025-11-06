# 🚀 Quick Deliver - 배달 최적화 시스템

배달앱 회사 포트폴리오용 MVP 프로젝트입니다. 라이더의 효율적인 배송과 관제센터의 배송 관리를 지원하는 백엔드 API를 제공합니다.

## 📋 프로젝트 개요

### 핵심 기능

1. **라이더 모바일 앱 API**
   - 라이더 등록 및 관리
   - 실시간 위치 추적
   - 배송 현황 관리
   - 근무 시간 관리
   - 개인 대시보드

2. **배송 관리 API**
   - 배송 생성 및 자동 배정
   - 실시간 배송 추적
   - 배송 상태 업데이트
   - 배송 이력 관리

3. **데이터 분석 및 최적화**
   - 배송망 효율성 분석
   - 라이더 성과 분석
   - 수요 예측
   - 경로 최적화

## 🛠️ 기술 스택

- **Backend**: Spring Boot 3.5.7
- **Database**: H2 (개발), PostgreSQL (프로덕션 준비)
- **Security**: Spring Security + JWT
- **Real-time**: WebSocket (STOMP)
- **Documentation**: Swagger/OpenAPI 3.0
- **Build Tool**: Gradle
- **Java Version**: 17

## 🗂️ 프로젝트 구조

```
quick-deliver/
├── src/main/java/com/delivery/quickdeliver/
│   ├── config/              # 설정 클래스
│   │   ├── DataLoader.java
│   │   ├── SwaggerConfig.java
│   │   └── WebConfig.java
│   ├── controller/          # REST API 컨트롤러
│   │   ├── RiderController.java
│   │   └── DeliveryController.java
│   ├── domain/
│   │   ├── entity/          # JPA 엔티티
│   │   │   ├── Rider.java
│   │   │   ├── Delivery.java
│   │   │   ├── DeliveryHistory.java
│   │   │   ├── RiderPerformance.java
│   │   │   ├── Merchant.java
│   │   │   └── Address.java
│   │   └── enums/           # Enum 타입
│   │       ├── DeliveryStatus.java
│   │       ├── RiderStatus.java
│   │       ├── Priority.java
│   │       └── VehicleType.java
│   ├── dto/
│   │   ├── request/         # 요청 DTO
│   │   └── response/        # 응답 DTO
│   ├── exception/           # 예외 처리
│   │   ├── GlobalExceptionHandler.java
│   │   ├── ResourceNotFoundException.java
│   │   ├── DuplicateResourceException.java
│   │   └── InvalidRequestException.java
│   ├── repository/          # JPA Repository
│   │   ├── RiderRepository.java
│   │   └── DeliveryRepository.java
│   └── service/             # 비즈니스 로직
│       ├── RiderService.java
│       ├── DeliveryService.java
│       ├── AnalyticsService.java
│       ├── RouteOptimizationService.java
│       ├── NotificationService.java
│       └── WebSocketService.java
└── src/main/resources/
    └── application.yml
```

## 🚀 시작하기

### 사전 요구사항

- Java 17 이상
- Gradle 8.x

### 실행 방법

1. **프로젝트 클론**
```bash
git clone [repository-url]
cd quick-deliver
```

2. **애플리케이션 실행**
```bash
./gradlew bootRun
```

3. **H2 콘솔 접속** (개발용)
```
URL: http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:testdb
Username: sa
Password: (빈 값)
```

4. **Swagger UI 접속**
```
http://localhost:8080/swagger-ui.html
```

## 📡 주요 API 엔드포인트

### 라이더 API

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/riders/register` | 라이더 등록 |
| POST | `/api/riders/{riderId}/shift/start` | 근무 시작 |
| POST | `/api/riders/{riderId}/shift/end` | 근무 종료 |
| PUT | `/api/riders/{riderId}/location` | 위치 업데이트 |
| GET | `/api/riders/{riderId}/dashboard` | 대시보드 조회 |
| GET | `/api/riders/{riderId}/deliveries` | 내 배송 목록 |

### 배송 API

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/deliveries` | 배송 생성 |
| PUT | `/api/deliveries/{deliveryId}/status` | 상태 업데이트 |
| GET | `/api/deliveries/{deliveryId}/track` | 배송 추적 |
| GET | `/api/deliveries/pending` | 대기 배송 목록 |
| POST | `/api/deliveries/{deliveryId}/cancel` | 배송 취소 |

## 🎯 주요 기능 상세

### 1. 자동 라이더 배정

배송 생성 시 다음 요소를 고려하여 최적의 라이더를 자동 배정합니다:
- 라이더와 픽업 위치 간 거리
- 라이더의 현재 상태 (AVAILABLE)
- 차량 타입별 최대 적재 무게
- 라이더의 평균 평점

### 2. 실시간 위치 추적

- 라이더의 위치를 실시간으로 업데이트
- WebSocket을 통해 관제센터에 브로드캐스트
- 배송 이력에 위치 정보 자동 기록

### 3. 배송 상태 관리

배송 상태 흐름:
```
PENDING → ASSIGNED → PICKING_UP → IN_TRANSIT → DELIVERED
                                              ↘ CANCELLED
```

### 4. 성과 분석

- 라이더별 일일/주간/월간 성과 분석
- 배송 효율성 지표 계산
- 시간대별 수요 패턴 분석
- 예측 분석 및 최적화 제안

## 💾 데이터 모델

### 주요 엔티티

- **Rider**: 라이더 정보, 위치, 상태, 성과 지표
- **Delivery**: 배송 정보, 주소, 상태, 경로
- **DeliveryHistory**: 배송 상태 변경 이력
- **RiderPerformance**: 라이더 일일 성과 데이터
- **Merchant**: 상점 정보

## 🧪 테스트 데이터

애플리케이션 시작 시 자동으로 테스트 데이터가 생성됩니다:
- 3명의 테스트 라이더
- 2건의 테스트 배송

## 📊 모니터링

Spring Boot Actuator를 통한 헬스 체크 및 메트릭:
```
http://localhost:8080/actuator/health
http://localhost:8080/actuator/metrics
http://localhost:8080/actuator/prometheus
```

## 🔐 보안

현재 Basic Authentication을 사용 중이며, JWT 기반 인증은 구현 예정입니다.

기본 인증 정보:
- Username: admin
- Password: admin123

## 🚧 TODO

- [ ] JWT 인증 구현
- [ ] WebSocket 설정 완료
- [ ] 백오피스 API 구현
- [ ] 분석 API 구현
- [ ] 단위 테스트 작성
- [ ] 통합 테스트 작성
- [ ] PostgreSQL 프로덕션 설정
- [ ] Docker 컨테이너화
- [ ] CI/CD 파이프라인

## 📝 API 문서

상세한 API 문서는 애플리케이션 실행 후 Swagger UI에서 확인할 수 있습니다:
http://localhost:8080/swagger-ui.html

## 🤝 기여

이 프로젝트는 포트폴리오 목적으로 제작되었습니다.

## 📄 라이선스

Apache 2.0

---

**개발자**: [Your Name]  
**이메일**: [Your Email]  
**개발 기간**: 2025.11
