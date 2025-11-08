# 🔐 인증 API 사용 예제

## 1. 회원가입

### 관리자 계정 생성
```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newadmin",
    "password": "password123",
    "name": "새 관리자",
    "email": "newadmin@example.com",
    "phoneNumber": "010-9999-9999",
    "role": "ADMIN"
  }'
```

**응답:**
```json
{
  "success": true,
  "message": "회원가입이 완료되었습니다",
  "data": null,
  "timestamp": "2025-11-08T10:00:00"
}
```

### 라이더 계정 생성
```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newrider",
    "password": "rider123",
    "name": "새 라이더",
    "email": "newrider@example.com",
    "phoneNumber": "010-8888-8888",
    "role": "RIDER"
  }'
```

---

## 2. 로그인

### 관리자 로그인
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

**응답:**
```json
{
  "success": true,
  "message": "로그인 성공",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTY5OTQzMjAwMCwiZXhwIjoxNjk5NTE4NDAwfQ.abcdef...",
    "type": "Bearer",
    "id": 1,
    "username": "admin",
    "name": "관리자",
    "email": "admin@quickdeliver.com",
    "role": "ADMIN"
  },
  "timestamp": "2025-11-08T10:05:00"
}
```

### 라이더 로그인
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "rider1",
    "password": "rider123"
  }'
```

**응답:**
```json
{
  "success": true,
  "message": "로그인 성공",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJyaWRlcjEiLCJpYXQiOjE2OTk0MzIwMDAsImV4cCI6MTY5OTUxODQwMH0.xyz123...",
    "type": "Bearer",
    "id": 3,
    "username": "rider1",
    "name": "김배달",
    "email": "rider1@quickdeliver.com",
    "role": "RIDER"
  },
  "timestamp": "2025-11-08T10:06:00"
}
```

---

## 3. 내 정보 조회

```bash
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..."
```

**응답:**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "id": 1,
    "username": "admin",
    "name": "관리자",
    "email": "admin@quickdeliver.com",
    "role": "ADMIN"
  },
  "timestamp": "2025-11-08T10:10:00"
}
```

---

## 4. JWT 토큰으로 보호된 API 호출

### 라이더 대시보드 조회
```bash
# 1. 먼저 로그인해서 토큰 받기
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"rider1","password":"rider123"}' \
  | jq -r '.data.token')

# 2. 토큰으로 API 호출
curl -X GET http://localhost:8080/api/riders/RDR-TEST001/dashboard \
  -H "Authorization: Bearer $TOKEN"
```

### 배송 생성 (인증 필요)
```bash
curl -X POST http://localhost:8080/api/deliveries \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "orderNumber": "ORD-20251108-001",
    "pickupAddress": {
      "address": "서울특별시 강남구 테헤란로 123",
      "latitude": 37.5012,
      "longitude": 127.0396,
      "contactName": "김상점",
      "contactPhone": "02-1234-5678"
    },
    "deliveryAddress": {
      "address": "서울특별시 강남구 역삼로 456",
      "latitude": 37.4979,
      "longitude": 127.0276,
      "contactName": "고객A",
      "contactPhone": "010-9876-5432"
    },
    "priority": "NORMAL",
    "itemDescription": "치킨 세트",
    "weight": 3.0,
    "quantity": 1,
    "deliveryFee": 3500
  }'
```

---

## 5. 에러 응답 예시

### 인증 실패 (401)
```bash
curl -X GET http://localhost:8080/api/riders/RDR-TEST001/dashboard
```

**응답:**
```json
{
  "timestamp": "2025-11-08T10:15:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource",
  "path": "/api/riders/RDR-TEST001/dashboard"
}
```

### 권한 부족 (403)
```bash
# 라이더 토큰으로 관리자 API 호출
curl -X GET http://localhost:8080/api/admin/dashboard \
  -H "Authorization: Bearer $RIDER_TOKEN"
```

**응답:**
```json
{
  "timestamp": "2025-11-08T10:16:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access is denied",
  "path": "/api/admin/dashboard"
}
```

### 잘못된 토큰
```bash
curl -X GET http://localhost:8080/api/riders/RDR-TEST001/dashboard \
  -H "Authorization: Bearer invalid-token"
```

**응답:**
```json
{
  "timestamp": "2025-11-08T10:17:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid JWT token",
  "path": "/api/riders/RDR-TEST001/dashboard"
}
```

### 중복 회원가입 (409)
```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "password",
    "name": "Test",
    "email": "test@example.com",
    "role": "ADMIN"
  }'
```

**응답:**
```json
{
  "timestamp": "2025-11-08T10:18:00",
  "status": 409,
  "error": "Conflict",
  "message": "이미 사용 중인 사용자명입니다",
  "path": "/api/auth/signup"
}
```

---

## 6. Postman 사용

### 환경 변수 설정
```
BASE_URL: http://localhost:8080
TOKEN: (로그인 후 자동 저장)
```

### 로그인 후 토큰 자동 저장
**Tests 탭에 추가:**
```javascript
if (pm.response.code === 200) {
    const jsonData = pm.response.json();
    pm.environment.set("TOKEN", jsonData.data.token);
}
```

### API 호출 시 토큰 사용
**Headers 탭:**
```
Authorization: Bearer {{TOKEN}}
```

---

## 7. 통합 시나리오 예시

### 전체 플로우: 회원가입 → 로그인 → API 호출

```bash
#!/bin/bash

# 1. 새 라이더 회원가입
echo "1. 회원가입..."
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testride r",
    "password": "test123",
    "name": "테스트라이더",
    "email": "testrider@example.com",
    "phoneNumber": "010-1111-2222",
    "role": "RIDER"
  }'

echo -e "\n\n2. 로그인..."
# 2. 로그인해서 토큰 받기
LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testrider",
    "password": "test123"
  }')

echo $LOGIN_RESPONSE

TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.data.token')
echo "Token: $TOKEN"

echo -e "\n\n3. 내 정보 조회..."
# 3. 내 정보 조회
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer $TOKEN"

echo -e "\n\n4. 가용 라이더 목록 조회..."
# 4. 가용 라이더 목록 조회
curl -X GET http://localhost:8080/api/riders/available \
  -H "Authorization: Bearer $TOKEN"

echo -e "\n\nDone!"
```

---

## 8. 토큰 디코딩 (디버깅용)

JWT 토큰을 디코딩하려면 https://jwt.io 사용

**토큰 예시:**
```
eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTY5OTQzMjAwMCwiZXhwIjoxNjk5NTE4NDAwfQ.signature
```

**디코딩 결과:**
```json
{
  "sub": "admin",
  "iat": 1699432000,
  "exp": 1699518400
}
```

- `sub`: username (subject)
- `iat`: 발급 시간 (issued at)
- `exp`: 만료 시간 (expiration)

---

## 테스트 계정 요약

| Username | Password | Role | 설명 |
|----------|----------|------|------|
| admin | admin123 | ADMIN | 관리자 - 모든 권한 |
| backoffice | backoffice123 | BACKOFFICE | 백오피스 - 관제 권한 |
| rider1 | rider123 | RIDER | 라이더 - 김배달 |
| rider2 | rider123 | RIDER | 라이더 - 이퀵 |
