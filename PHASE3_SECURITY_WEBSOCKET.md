# 🔐 Phase 3: Security + WebSocket 구현 완료

## ✅ 구현 완료 항목

### 1. JWT 기반 인증 시스템

#### 구현된 컴포넌트:
- ✅ `User` 엔티티 (UserDetails 구현)
- ✅ `UserRepository`
- ✅ `JwtTokenProvider` - JWT 토큰 생성 및 검증
- ✅ `JwtAuthenticationFilter` - JWT 인증 필터
- ✅ `CustomUserDetailsService` - 사용자 인증
- ✅ `JwtAuthenticationEntryPoint` - 인증 실패 핸들러
- ✅ `SecurityConfig` - Spring Security 설정

#### 기능:
- ✅ JWT 토큰 생성 및 검증
- ✅ Bearer 토큰 인증
- ✅ 역할 기반 접근 제어 (ROLE_ADMIN, ROLE_BACKOFFICE, ROLE_RIDER)
- ✅ Stateless 세션 관리
- ✅ Password 암호화 (BCrypt)

---

### 2. WebSocket 실시간 통신

#### 구현된 컴포넌트:
- ✅ `WebSocketConfig` - STOMP 설정
- ✅ `WebSocketEventListener` - 연결/해제 이벤트 처리
- ✅ `WebSocketController` - 메시지 핸들러
- ✅ WebSocket 테스트 페이지 (`websocket-test.html`)

#### 기능:
- ✅ STOMP over WebSocket
- ✅ SockJS Fallback 지원
- ✅ 라이더 위치 실시간 브로드캐스트
- ✅ 배송 상태 실시간 업데이트
- ✅ 1:1 개인 메시징 (User Queue)
- ✅ 1:N 브로드캐스팅 (Topic)
- ✅ 채팅 메시지 전송

---

### 3. Auth API

#### 엔드포인트:

| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| POST | `/api/auth/signup` | 회원가입 | Public |
| POST | `/api/auth/login` | 로그인 | Public |
| GET | `/api/auth/me` | 내 정보 조회 | Authenticated |

#### Request/Response 예시:

**회원가입:**
```json
POST /api/auth/signup
{
  "username": "testuser",
  "password": "password123",
  "name": "테스트 사용자",
  "email": "test@example.com",
  "phoneNumber": "010-1234-5678",
  "role": "RIDER"
}
```

**로그인:**
```json
POST /api/auth/login
{
  "username": "admin",
  "password": "admin123"
}

Response:
{
  "success": true,
  "message": "로그인 성공",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "type": "Bearer",
    "id": 1,
    "username": "admin",
    "name": "관리자",
    "email": "admin@quickdeliver.com",
    "role": "ADMIN"
  }
}
```

---

## 🔑 테스트 계정

애플리케이션 시작 시 자동으로 생성되는 테스트 계정:

| Username | Password | Role | 설명 |
|----------|----------|------|------|
| `admin` | `admin123` | ADMIN | 관리자 |
| `backoffice` | `backoffice123` | BACKOFFICE | 백오피스 직원 |
| `rider1` | `rider123` | RIDER | 라이더 (김배달) |
| `rider2` | `rider123` | RIDER | 라이더 (이퀵) |

---

## 🚀 사용 방법

### 1. JWT 인증 사용하기

#### 1단계: 로그인
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

#### 2단계: 토큰 받기
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTY5...",
  "type": "Bearer"
}
```

#### 3단계: API 호출 시 토큰 사용
```bash
curl -X GET http://localhost:8080/api/riders/RDR-TEST001/dashboard \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..."
```

---

### 2. WebSocket 연결하기

#### 브라우저에서 테스트:
```
http://localhost:8080/websocket-test.html
```

#### JavaScript 코드 예시:
```javascript
// 1. SockJS + STOMP 연결
const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
    console.log('Connected: ' + frame);
    
    // 2. 구독 설정
    // 라이더 위치 업데이트 구독
    stompClient.subscribe('/topic/monitoring/riders', function(message) {
        console.log('Rider location:', JSON.parse(message.body));
    });
    
    // 배송 상태 업데이트 구독
    stompClient.subscribe('/topic/monitoring/deliveries', function(message) {
        console.log('Delivery status:', JSON.parse(message.body));
    });
    
    // 개인 메시지 구독
    stompClient.subscribe('/user/RDR-TEST001/queue/urgent', function(message) {
        console.log('Urgent message:', JSON.parse(message.body));
    });
});

// 3. 메시지 전송
// 라이더 위치 전송
stompClient.send("/app/rider/location", {}, JSON.stringify({
    riderId: 'RDR-TEST001',
    latitude: 37.5665,
    longitude: 126.9780,
    timestamp: Date.now()
}));

// 배송 상태 전송
stompClient.send("/app/delivery/status", {}, JSON.stringify({
    deliveryId: 'DEL-TEST001',
    status: 'IN_TRANSIT',
    riderId: 'RDR-TEST001',
    timestamp: Date.now()
}));
```

---

## 📡 WebSocket 토픽/큐

### Topic (1:N 브로드캐스트)
- `/topic/monitoring/riders` - 모든 라이더 위치 업데이트
- `/topic/monitoring/deliveries` - 모든 배송 상태 업데이트
- `/topic/monitoring/disconnect` - 연결 해제 알림

### Queue (1:1 개인 메시지)
- `/user/{riderId}/queue/connection` - 연결 확인 메시지
- `/user/{riderId}/queue/urgent` - 긴급 알림
- `/user/{riderId}/queue/chat` - 채팅 메시지

### Application Destinations (클라이언트 → 서버)
- `/app/rider/location` - 라이더 위치 전송
- `/app/delivery/status` - 배송 상태 전송
- `/app/rider/connect` - 라이더 연결 등록
- `/app/admin/urgent` - 긴급 메시지 전송 (관리자)
- `/app/chat/send` - 채팅 메시지 전송

---

## 🔒 권한 설정

### API 접근 권한:

| 엔드포인트 | 권한 |
|-----------|------|
| `/api/auth/**` | Public |
| `/api/admin/**` | ADMIN |
| `/api/backoffice/**` | ADMIN, BACKOFFICE |
| `/api/riders/**` | ADMIN, BACKOFFICE, RIDER |
| `/api/deliveries/**` | Authenticated |
| `/ws/**` | Public |
| `/swagger-ui/**` | Public |
| `/h2-console/**` | Public (개발용) |

---

## 🧪 테스트 시나리오

### 시나리오 1: 인증 흐름
1. 회원가입: POST `/api/auth/signup`
2. 로그인: POST `/api/auth/login`
3. 토큰 받기
4. 보호된 API 호출 (Authorization 헤더 포함)
5. 내 정보 조회: GET `/api/auth/me`

### 시나리오 2: 실시간 배송 추적
1. WebSocket 연결
2. `/topic/monitoring/deliveries` 구독
3. 라이더가 배송 상태 업데이트
4. 관제센터에서 실시간으로 상태 수신
5. 고객 앱에서 실시간 위치 확인

### 시나리오 3: 라이더 위치 추적
1. WebSocket 연결
2. `/topic/monitoring/riders` 구독
3. 라이더 위치 주기적 전송 (5초마다)
4. 관제센터 지도에 실시간 표시
5. 배송 배정 시 거리 계산에 활용

---

## 🛠️ 기술 상세

### JWT 설정 (application.yml)
```yaml
jwt:
  secret: 404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
  expiration: 86400000 # 24 hours
```

### Security 기능
- ✅ BCrypt 비밀번호 암호화
- ✅ JWT 토큰 기반 인증
- ✅ Stateless 세션
- ✅ CORS 설정
- ✅ CSRF 비활성화 (REST API)
- ✅ Role-based Access Control

### WebSocket 기능
- ✅ STOMP Protocol
- ✅ SockJS Fallback
- ✅ Message Broker
- ✅ User Destinations
- ✅ Connection Event Handling

---

## 📊 아키텍처

```
┌─────────────┐
│ Client App  │
└──────┬──────┘
       │ HTTP + JWT
       │ WebSocket
       ▼
┌─────────────────────────┐
│ Spring Security Filter  │
│ - JWT Authentication    │
└──────────┬──────────────┘
           │
┌──────────▼──────────────┐
│   Controllers           │
│ - AuthController        │
│ - RiderController       │
│ - DeliveryController    │
└──────────┬──────────────┘
           │
┌──────────▼──────────────┐
│   WebSocket Layer       │
│ - STOMP Broker          │
│ - Message Handler       │
└──────────┬──────────────┘
           │
┌──────────▼──────────────┐
│   Services              │
└──────────┬──────────────┘
           │
┌──────────▼──────────────┐
│   Database              │
└─────────────────────────┘
```

---

## 🐛 트러블슈팅

### 1. JWT 토큰 인증 실패
- 토큰 형식 확인: `Bearer {token}`
- 토큰 만료 확인 (24시간)
- Secret 키 일치 확인

### 2. WebSocket 연결 실패
- CORS 설정 확인
- 방화벽 설정 확인
- SockJS fallback 사용 확인

### 3. 권한 거부 (403)
- 사용자 role 확인
- Security 설정의 권한 규칙 확인
- JWT 토큰의 권한 정보 확인

---

## 📝 다음 단계

Phase 3 완료! 🎉

이제 다음을 구현할 수 있습니다:
- [ ] Phase 4: BackOffice API
- [ ] Phase 5: Analytics API
- [ ] Phase 6: 테스트 코드
- [ ] Phase 7: 배포 설정

---

**작성일**: 2025-11-08
**버전**: 1.0.0
**상태**: Phase 3 완료 ✅
