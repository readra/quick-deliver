# 🚀 Quick Deliver - 배달 최적화 시스템

배달앱 회사 포트폴리오용 MVP 프로젝트입니다. 라이더의 효율적인 배송과 관제센터의 배송 관리를 지원하는 백엔드 API를 제공합니다.

## 📋 프로젝트 개요

### 핵심 기능

1. **JWT 기반 인증 시스템** 🔐
   - 회원가입 및 로그인
   - JWT 토큰 기반 인증
   - 역할 기반 접근 제어 (ADMIN, BACKOFFICE, RIDER, CUSTOMER)
   - BCrypt 비밀번호 암호화
   - Stateless 세션 관리

2. **라이더 모바일 앱 API** 📱
   - 라이더 등록 및 관리
   - 실시간 위치 추적
   - 배송 현황 관리
   - 근무 시간 관리 (출/퇴근)
   - 개인 대시보드

3. **배송 관리 API** 📦
   - 배송 생성 및 자동 배정
   - 실시간 배송 추적
   - 배송 상태 업데이트
   - 배송 이력 관리
   - 배송 평가 시스템

4. **실시간 통신 (WebSocket)** 📡
   - STOMP 프로토콜 기반 실시간 메시징
   - 라이더 위치 실시간 브로드캐스트
   - 배송 상태 실시간 업데이트
   - 1:1 개인 메시징
   - 채팅 및 긴급 알림

5. **데이터 분석 및 최적화** 📊
   - 배송망 효율성 분석
   - 라이더 성과 분석
   - 수요 예측
   - 경로 최적화
   - 실시간 모니터링

## 🛠️ 기술 스택

- **Backend**: Spring Boot 3.5.7
- **Database**: H2 (개발), PostgreSQL (프로덕션)
- **Security**: Spring Security 6.x + JWT (jjwt 0.11.5)
- **Real-time**: WebSocket + STOMP + SockJS
- **Documentation**: SpringDoc OpenAPI 3.0 (Swagger UI)
- **Monitoring**: Spring Boot Actuator + Prometheus
- **Build Tool**: Gradle 8.x
- **Java Version**: 17
- **Others**: Lombok, MapStruct, Validation

## 🗂️ 프로젝트 구조

```
quick-deliver/
├── src/main/java/com/delivery/quickdeliver/
│   ├── config/              # 설정 클래스
│   │   ├── DataLoader.java
│   │   ├── SecurityConfig.java
│   │   ├── SwaggerConfig.java
│   │   ├── WebConfig.java
│   │   ├── WebSocketConfig.java
│   │   └── WebSocketEventListener.java
│   ├── controller/          # REST API 컨트롤러
│   │   ├── AuthController.java
│   │   ├── RiderController.java
│   │   ├── DeliveryController.java
│   │   └── WebSocketController.java
│   ├── domain/
│   │   ├── entity/          # JPA 엔티티
│   │   │   ├── User.java
│   │   │   ├── Rider.java
│   │   │   ├── Delivery.java
│   │   │   ├── DeliveryHistory.java
│   │   │   ├── RiderPerformance.java
│   │   │   ├── Merchant.java
│   │   │   └── Address.java
│   │   └── enums/           # Enum 타입
│   │       ├── UserRole.java
│   │       ├── DeliveryStatus.java
│   │       ├── RiderStatus.java
│   │       ├── Priority.java
│   │       └── VehicleType.java
│   ├── dto/
│   │   ├── request/         # 요청 DTO
│   │   │   ├── LoginRequest.java
│   │   │   ├── SignupRequest.java
│   │   │   ├── RiderRegisterRequest.java
│   │   │   ├── DeliveryCreateRequest.java
│   │   │   └── DeliveryStatusUpdateRequest.java
│   │   └── response/        # 응답 DTO
│   │       ├── JwtResponse.java
│   │       └── RiderResponse.java
│   ├── exception/           # 예외 처리
│   │   ├── GlobalExceptionHandler.java
│   │   ├── ResourceNotFoundException.java
│   │   ├── DuplicateResourceException.java
│   │   └── InvalidRequestException.java
│   ├── repository/          # JPA Repository
│   │   ├── UserRepository.java
│   │   ├── RiderRepository.java
│   │   └── DeliveryRepository.java
│   ├── security/            # 보안 관련
│   │   ├── JwtTokenProvider.java
│   │   ├── JwtAuthenticationFilter.java
│   │   ├── CustomUserDetailsService.java
│   │   └── JwtAuthenticationEntryPoint.java
│   └── service/             # 비즈니스 로직
│       ├── RiderService.java
│       ├── DeliveryService.java
│       ├── AnalyticsService.java
│       ├── BackOfficeService.java
│       ├── RouteOptimizationService.java
│       ├── NotificationService.java
│       └── WebSocketService.java
└── src/main/resources/
    ├── application.yml
    └── static/
        └── websocket-test.html
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

5. **WebSocket 테스트 페이지**
```
http://localhost:8080/websocket-test.html
```

## 🔑 테스트 계정

애플리케이션 시작 시 자동으로 생성되는 테스트 계정:

| Username | Password | Role | 설명 |
|----------|----------|------|------|
| `admin` | `admin123` | ADMIN | 시스템 관리자 |
| `backoffice` | `backoffice123` | BACKOFFICE | 관제 담당자 |
| `rider1` | `rider123` | RIDER | 라이더 김배달 |
| `rider2` | `rider123` | RIDER | 라이더 이퀵 |

## 📡 주요 API 엔드포인트

### 인증 API

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/auth/signup` | 회원가입 | Public |
| POST | `/api/auth/login` | 로그인 (JWT 발급) | Public |
| GET | `/api/auth/me` | 내 정보 조회 | Required |

### 라이더 API

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/riders/register` | 라이더 등록 | ADMIN, BACKOFFICE |
| POST | `/api/riders/{riderId}/shift/start` | 근무 시작 | RIDER |
| POST | `/api/riders/{riderId}/shift/end` | 근무 종료 | RIDER |
| PUT | `/api/riders/{riderId}/location` | 위치 업데이트 | RIDER |
| PUT | `/api/riders/{riderId}/status` | 상태 변경 | RIDER |
| GET | `/api/riders/{riderId}/dashboard` | 대시보드 조회 | RIDER |
| GET | `/api/riders/{riderId}/deliveries` | 내 배송 목록 | RIDER |
| GET | `/api/riders/{riderId}/deliveries/active` | 활성 배송 조회 | RIDER |
| GET | `/api/riders/available` | 가용 라이더 목록 | BACKOFFICE |
| GET | `/api/riders/active` | 근무중 라이더 목록 | BACKOFFICE |

### 배송 API

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/deliveries` | 배송 생성 | Authenticated |
| PUT | `/api/deliveries/{deliveryId}/status` | 상태 업데이트 | RIDER |
| GET | `/api/deliveries/{deliveryId}/track` | 배송 추적 | Authenticated |
| GET | `/api/deliveries/pending` | 대기 배송 목록 | BACKOFFICE |
| POST | `/api/deliveries/{deliveryId}/cancel` | 배송 취소 | BACKOFFICE |
| POST | `/api/deliveries/{deliveryId}/rating` | 배송 평가 | Authenticated |

### WebSocket 엔드포인트

**연결 엔드포인트**
```
ws://localhost:8080/ws
```

**구독 (Subscribe) Topics**
- `/topic/monitoring/riders` - 라이더 위치 실시간 업데이트
- `/topic/monitoring/deliveries` - 배송 상태 실시간 업데이트
- `/user/{riderId}/queue/connection` - 개인 연결 확인
- `/user/{riderId}/queue/urgent` - 긴급 알림
- `/user/{riderId}/queue/chat` - 채팅 메시지

**발행 (Publish) Destinations**
- `/app/rider/location` - 라이더 위치 전송
- `/app/delivery/status` - 배송 상태 전송
- `/app/rider/connect` - 라이더 연결 등록
- `/app/admin/urgent` - 긴급 메시지 전송
- `/app/chat/send` - 채팅 메시지 전송

## 🎯 주요 기능 상세

### 1. JWT 기반 인증 시스템 🔐

**보안 기능**
- JWT 토큰 기반 Stateless 인증
- BCrypt를 사용한 비밀번호 암호화
- 토큰 만료 시간 관리 (24시간)
- 역할 기반 접근 제어 (RBAC)

**사용 예시**
```bash
# 1. 로그인하여 JWT 토큰 발급
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# 2. 발급받은 토큰으로 API 호출
curl -X GET http://localhost:8080/api/riders/available \
  -H "Authorization: Bearer {token}"
```

**권한 구조**
- **ADMIN**: 모든 API 접근 가능
- **BACKOFFICE**: 관제 및 배송 관리 API 접근
- **RIDER**: 라이더 관련 API 접근
- **CUSTOMER**: 고객 관련 API 접근 (향후 구현)

### 2. 실시간 통신 (WebSocket) 📡

**STOMP over WebSocket**
- SockJS Fallback 지원으로 브라우저 호환성 확보
- 실시간 양방향 통신
- 토픽 기반 브로드캐스팅
- 개인 메시지 큐

**실시간 기능**
- 라이더 위치 자동 브로드캐스트
- 배송 상태 변경 즉시 알림
- 관제센터 <-> 라이더 실시간 채팅
- 긴급 알림 푸시

**WebSocket 연결 예시**
```javascript
const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
    // 라이더 위치 구독
    stompClient.subscribe('/topic/monitoring/riders', function(message) {
        const location = JSON.parse(message.body);
        console.log('라이더 위치:', location);
    });
    
    // 위치 정보 전송
    stompClient.send("/app/rider/location", {}, JSON.stringify({
        riderId: 'RDR-TEST001',
        latitude: 37.5665,
        longitude: 126.9780
    }));
});
```

### 3. 자동 라이더 배정

배송 생성 시 다음 요소를 고려하여 최적의 라이더를 자동 배정합니다:
- 라이더와 픽업 위치 간 거리
- 라이더의 현재 상태 (AVAILABLE)
- 차량 타입별 최대 적재 무게
- 라이더의 평균 평점

### 4. 실시간 위치 추적

- 라이더의 위치를 실시간으로 업데이트
- WebSocket을 통해 관제센터에 브로드캐스트
- 배송 이력에 위치 정보 자동 기록

### 5. 배송 상태 관리

배송 상태 흐름:
```
PENDING → ASSIGNED → PICKING_UP → IN_TRANSIT → DELIVERED
                                              ↘ CANCELLED
```

### 6. 성과 분석

- 라이더별 일일/주간/월간 성과 분석
- 배송 효율성 지표 계산
- 시간대별 수요 패턴 분석
- 예측 분석 및 최적화 제안

## 💾 데이터 모델

### 주요 엔티티

- **User**: 시스템 사용자 (인증, 역할 관리)
- **Rider**: 라이더 정보, 위치, 상태, 성과 지표
- **Delivery**: 배송 정보, 주소, 상태, 경로
- **DeliveryHistory**: 배송 상태 변경 이력
- **RiderPerformance**: 라이더 일일 성과 데이터
- **Merchant**: 상점 정보

## 🧪 테스트

### 테스트 데이터

애플리케이션 시작 시 자동으로 생성되는 데이터:
- 4명의 테스트 사용자 (admin, backoffice, rider1, rider2)
- 3명의 테스트 라이더
- 2건의 테스트 배송

### WebSocket 테스트

브라우저에서 `http://localhost:8080/websocket-test.html` 접속하여 실시간 통신 테스트 가능

### API 테스트

상세한 API 사용 예제는 다음 문서 참조:
- `API_EXAMPLES.md` - 일반 API 예제
- `AUTH_API_EXAMPLES.md` - 인증 API 예제

## 📊 모니터링

Spring Boot Actuator를 통한 헬스 체크 및 메트릭:
```
http://localhost:8080/actuator/health
http://localhost:8080/actuator/metrics
http://localhost:8080/actuator/prometheus
```

## 🔐 보안

### 구현된 보안 기능

- ✅ JWT 토큰 기반 Stateless 인증
- ✅ BCrypt 비밀번호 암호화 (강도 10)
- ✅ 역할 기반 접근 제어 (RBAC)
- ✅ CORS 설정 (개발 환경: 모든 출처 허용)
- ✅ 인증 실패 예외 처리
- ✅ Spring Security 6.x 최신 보안 설정

### 엔드포인트 접근 권한

| 엔드포인트 | 권한 |
|-----------|------|
| `/api/auth/**` | Public |
| `/api/admin/**` | ADMIN |
| `/api/backoffice/**` | ADMIN, BACKOFFICE |
| `/api/riders/**` | ADMIN, BACKOFFICE, RIDER |
| `/api/deliveries/**` | Authenticated |
| `/ws/**` | Public |
| `/swagger-ui/**` | Public |
| `/h2-console/**` | Public (개발 환경) |

### JWT 토큰 설정

```yaml
jwt:
  secret: [Application Secret Key]
  expiration: 86400000  # 24시간 (밀리초)
```

## 🚧 개발 로드맵

### ✅ Phase 1: 기본 구조 및 도메인 모델 (완료)
- [x] 프로젝트 초기 설정
- [x] 도메인 엔티티 설계
- [x] Repository 계층 구현
- [x] 기본 API 엔드포인트

### ✅ Phase 2: 핵심 기능 구현 (완료)
- [x] 라이더 관리 API
- [x] 배송 관리 API
- [x] 자동 배정 로직
- [x] 실시간 위치 추적
- [x] 성과 분석 기능

### ✅ Phase 3: Security & WebSocket (완료)
- [x] JWT 인증 시스템
- [x] 역할 기반 접근 제어
- [x] WebSocket 실시간 통신
- [x] STOMP 프로토콜 구현
- [x] 실시간 위치/상태 브로드캐스팅

### 🚧 Phase 4: BackOffice API (진행 예정)
- [ ] 전체 대시보드 API
- [ ] 라이더 관리 API
- [ ] 배송 관리 API
- [ ] 통계 및 리포트 API

### 📋 Phase 5: Analytics & Optimization (계획)
- [ ] 실시간 분석 대시보드
- [ ] 예측 분석 알고리즘
- [ ] 경로 최적화 개선
- [ ] 성과 리포트 자동화

### 🧪 Phase 6: Testing & Deployment (계획)
- [ ] 단위 테스트 작성
- [ ] 통합 테스트 작성
- [ ] E2E 테스트
- [ ] PostgreSQL 프로덕션 설정
- [ ] Docker 컨테이너화
- [ ] CI/CD 파이프라인 구축

## 📝 API 문서

### Swagger UI
상세한 API 문서는 애플리케이션 실행 후 Swagger UI에서 확인할 수 있습니다:
```
http://localhost:8080/swagger-ui.html
```

### 추가 문서
- `PHASE3_COMPLETE_SUMMARY.md` - Phase 3 구현 완료 요약
- `PHASE3_SECURITY_WEBSOCKET.md` - Security & WebSocket 상세 가이드
- `API_EXAMPLES.md` - REST API 사용 예제
- `AUTH_API_EXAMPLES.md` - 인증 API 사용 예제
- `IMPLEMENTATION_SUMMARY.md` - 전체 구현 요약

## 📊 시스템 아키텍처

```
┌──────────────────┐
│   Client App     │
│ (Web/Mobile)     │
└────────┬─────────┘
         │
         │ HTTP REST API
         │ + JWT Token
         │ WebSocket/STOMP
         │
┌────────▼─────────────────────┐
│   Spring Security Filter     │
│   - JwtAuthenticationFilter  │
│   - CORS Filter              │
└────────┬─────────────────────┘
         │
┌────────▼─────────────────────┐
│   Controllers                │
│   - AuthController           │
│   - RiderController          │
│   - DeliveryController       │
│   - WebSocketController      │
└────────┬─────────────────────┘
         │
┌────────▼─────────────────────┐
│   WebSocket Message Broker   │
│   - STOMP Protocol           │
│   - SockJS Fallback          │
└────────┬─────────────────────┘
         │
┌────────▼─────────────────────┐
│   Services                   │
│   - RiderService             │
│   - DeliveryService          │
│   - WebSocketService         │
│   - AnalyticsService         │
└────────┬─────────────────────┘
         │
┌────────▼─────────────────────┐
│   Repositories (JPA)         │
│   - UserRepository           │
│   - RiderRepository          │
│   - DeliveryRepository       │
└────────┬─────────────────────┘
         │
┌────────▼─────────────────────┐
│   Database (H2)   │
│   - users                    │
│   - riders                   │
│   - deliveries               │
└──────────────────────────────┘
```

## 🎓 학습 포인트

이 프로젝트를 통해 다음 기술들을 학습하고 적용했습니다:

### Backend 개발
- Spring Boot 3.x 최신 기능 활용
- Spring Security 6.x 보안 설정
- JWT 기반 Stateless 인증 구현
- WebSocket + STOMP 실시간 통신
- RESTful API 설계 및 구현
- JPA/Hibernate를 활용한 데이터베이스 설계

### 실무 패턴
- 계층화 아키텍처 (Controller-Service-Repository)
- DTO 패턴을 활용한 계층 분리
- 전역 예외 처리 (Global Exception Handler)
- 역할 기반 접근 제어 (RBAC)
- 실시간 이벤트 기반 아키텍처

### 문서화 및 테스트
- OpenAPI 3.0 (Swagger) 문서화
- 상세한 기술 문서 작성
- 테스트 가능한 구조 설계

## 🤝 기여

이 프로젝트는 포트폴리오 목적으로 제작되었습니다.

개선 사항이나 버그를 발견하시면 이슈를 등록해 주세요.

## 📄 라이선스

이 프로젝트는 Apache License 2.0 라이선스 하에 배포됩니다.

---

## 👨‍💻 개발자 정보

**개발자**: readra  
**이메일**: yong9976@naver.com  
**개발 기간**: 2025.11  
**프로젝트 상태**: Phase 3 완료 (Security & WebSocket)

### 기술 스택 요약
- Backend: Spring Boot 3.5.7, Spring Security 6.x, JWT
- Database: H2
- Real-time: WebSocket, STOMP, SockJS
- Monitoring: Actuator, Prometheus
- Documentation: Swagger/OpenAPI 3.0

### 주요 성과
- ✅ JWT 기반 인증 시스템 구현
- ✅ 역할 기반 접근 제어 (RBAC) 구현
- ✅ WebSocket 실시간 양방향 통신 구현
- ✅ 자동 라이더 배정 알고리즘 구현
- ✅ RESTful API 설계 및 19개 엔드포인트 구현
- ✅ 상세한 기술 문서 작성
