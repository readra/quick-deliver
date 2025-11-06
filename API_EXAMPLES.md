# 📡 API 사용 예제

## 라이더 API

### 1. 라이더 등록

```bash
POST http://localhost:8080/api/riders/register
Content-Type: application/json

{
  "name": "홍길동",
  "phoneNumber": "010-1234-5678",
  "email": "hong@example.com",
  "vehicleType": "MOTORCYCLE",
  "vehicleNumber": "12가3456"
}
```

**응답 예시:**
```json
{
  "success": true,
  "message": "라이더 등록이 완료되었습니다.",
  "data": {
    "id": 1,
    "riderId": "RDR-ABC12345",
    "name": "홍길동",
    "phoneNumber": "010-1234-5678",
    "email": "hong@example.com",
    "status": "OFFLINE",
    "vehicleType": "MOTORCYCLE",
    "vehicleNumber": "12가3456",
    "totalDeliveries": 0,
    "averageRating": 5.0
  },
  "timestamp": "2025-11-06T10:30:00"
}
```

### 2. 근무 시작

```bash
POST http://localhost:8080/api/riders/RDR-ABC12345/shift/start
```

**응답:**
```json
{
  "success": true,
  "message": "근무가 시작되었습니다.",
  "timestamp": "2025-11-06T10:31:00"
}
```

### 3. 위치 업데이트

```bash
PUT http://localhost:8080/api/riders/RDR-ABC12345/location
Content-Type: application/json

{
  "latitude": 37.5665,
  "longitude": 126.9780
}
```

### 4. 대시보드 조회

```bash
GET http://localhost:8080/api/riders/RDR-ABC12345/dashboard
```

**응답 예시:**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "riderId": "RDR-ABC12345",
    "name": "홍길동",
    "status": "AVAILABLE",
    "todayDeliveries": 12,
    "totalDeliveries": 245,
    "averageRating": 4.8,
    "averageDeliveryTime": 28.5,
    "currentLocation": {
      "latitude": 37.5665,
      "longitude": 126.9780
    },
    "shiftStartTime": "2025-11-06T10:31:00"
  },
  "timestamp": "2025-11-06T15:00:00"
}
```

### 5. 내 배송 목록 조회

```bash
GET http://localhost:8080/api/riders/RDR-ABC12345/deliveries
```

### 6. 근무 종료

```bash
POST http://localhost:8080/api/riders/RDR-ABC12345/shift/end
```

---

## 배송 API

### 1. 배송 생성

```bash
POST http://localhost:8080/api/deliveries
Content-Type: application/json

{
  "orderNumber": "ORD-20251106-001",
  "pickupAddress": {
    "address": "서울특별시 강남구 테헤란로 123",
    "detailAddress": "ABC빌딩 1층",
    "latitude": 37.5012,
    "longitude": 127.0396,
    "contactName": "김상점",
    "contactPhone": "02-1234-5678"
  },
  "deliveryAddress": {
    "address": "서울특별시 강남구 역삼로 456",
    "detailAddress": "DEF아파트 101동 1001호",
    "latitude": 37.4979,
    "longitude": 127.0276,
    "contactName": "고객A",
    "contactPhone": "010-9876-5432"
  },
  "priority": "NORMAL",
  "itemDescription": "치킨 2마리 + 콜라 2병",
  "weight": 3.5,
  "quantity": 1,
  "deliveryFee": 3500,
  "specialInstructions": "문 앞에 두고 벨 눌러주세요"
}
```

**응답 예시:**
```json
{
  "success": true,
  "message": "배송이 생성되었습니다.",
  "data": {
    "id": 1,
    "deliveryId": "DEL-XYZ78901",
    "orderNumber": "ORD-20251106-001",
    "status": "ASSIGNED",
    "priority": "NORMAL",
    "itemDescription": "치킨 2마리 + 콜라 2병",
    "weight": 3.5,
    "quantity": 1,
    "deliveryFee": 3500,
    "estimatedDistance": 2.5,
    "estimatedDeliveryTime": "2025-11-06T16:00:00",
    "riderId": "RDR-ABC12345",
    "riderName": "홍길동"
  },
  "timestamp": "2025-11-06T15:00:00"
}
```

### 2. 배송 상태 업데이트

#### 픽업 시작
```bash
PUT http://localhost:8080/api/deliveries/DEL-XYZ78901/status
Content-Type: application/json

{
  "status": "PICKING_UP",
  "latitude": 37.5012,
  "longitude": 127.0396
}
```

#### 배송 중
```bash
PUT http://localhost:8080/api/deliveries/DEL-XYZ78901/status
Content-Type: application/json

{
  "status": "IN_TRANSIT",
  "latitude": 37.5000,
  "longitude": 127.0300
}
```

#### 배송 완료
```bash
PUT http://localhost:8080/api/deliveries/DEL-XYZ78901/status
Content-Type: application/json

{
  "status": "DELIVERED",
  "latitude": 37.4979,
  "longitude": 127.0276
}
```

### 3. 배송 추적

```bash
GET http://localhost:8080/api/deliveries/DEL-XYZ78901/track
```

**응답 예시:**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "deliveryId": "DEL-XYZ78901",
    "currentStatus": "IN_TRANSIT",
    "currentLocation": {
      "latitude": 37.5000,
      "longitude": 127.0300
    },
    "estimatedDeliveryTime": "2025-11-06T16:00:00",
    "histories": [
      {
        "event": "배송 생성",
        "status": "PENDING",
        "eventTime": "2025-11-06T15:00:00",
        "location": null
      },
      {
        "event": "라이더 배정",
        "status": "ASSIGNED",
        "eventTime": "2025-11-06T15:01:00",
        "location": null
      },
      {
        "event": "Status updated to PICKING_UP",
        "status": "PICKING_UP",
        "eventTime": "2025-11-06T15:10:00",
        "location": {
          "latitude": 37.5012,
          "longitude": 127.0396
        }
      },
      {
        "event": "Status updated to IN_TRANSIT",
        "status": "IN_TRANSIT",
        "eventTime": "2025-11-06T15:20:00",
        "location": {
          "latitude": 37.5000,
          "longitude": 127.0300
        }
      }
    ]
  },
  "timestamp": "2025-11-06T15:25:00"
}
```

### 4. 대기 중인 배송 목록

```bash
GET http://localhost:8080/api/deliveries/pending
```

### 5. 배송 취소

```bash
POST http://localhost:8080/api/deliveries/DEL-XYZ78901/cancel?reason=고객%20요청
```

---

## 기타 API

### 가용 라이더 목록

```bash
GET http://localhost:8080/api/riders/available
```

**응답 예시:**
```json
{
  "success": true,
  "message": "Success",
  "data": [
    {
      "riderId": "RDR-ABC12345",
      "name": "홍길동",
      "status": "AVAILABLE",
      "vehicleType": "MOTORCYCLE",
      "currentLatitude": 37.5665,
      "currentLongitude": 126.9780,
      "averageRating": 4.8,
      "totalDeliveries": 245
    },
    {
      "riderId": "RDR-DEF67890",
      "name": "김배달",
      "status": "AVAILABLE",
      "vehicleType": "BIKE",
      "currentLatitude": 37.5700,
      "currentLongitude": 126.9800,
      "averageRating": 4.9,
      "totalDeliveries": 312
    }
  ],
  "timestamp": "2025-11-06T15:30:00"
}
```

### 근무 중인 라이더 목록

```bash
GET http://localhost:8080/api/riders/active
```

---

## 에러 응답 예시

### 404 Not Found
```json
{
  "timestamp": "2025-11-06T15:35:00",
  "status": 404,
  "error": "Not Found",
  "message": "Rider not found",
  "path": "/api/riders/RDR-INVALID"
}
```

### 400 Bad Request (Validation Error)
```json
{
  "timestamp": "2025-11-06T15:40:00",
  "status": 400,
  "error": "Validation Failed",
  "message": "입력값 검증에 실패했습니다.",
  "path": "/api/riders/register",
  "fieldErrors": [
    {
      "field": "email",
      "message": "올바른 이메일 형식이 아닙니다",
      "rejectedValue": "invalid-email"
    },
    {
      "field": "phoneNumber",
      "message": "올바른 전화번호 형식이 아닙니다",
      "rejectedValue": "123456"
    }
  ]
}
```

### 409 Conflict
```json
{
  "timestamp": "2025-11-06T15:45:00",
  "status": 409,
  "error": "Conflict",
  "message": "Email already exists",
  "path": "/api/riders/register"
}
```

---

## cURL 예제

### 라이더 등록
```bash
curl -X POST http://localhost:8080/api/riders/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "홍길동",
    "phoneNumber": "010-1234-5678",
    "email": "hong@example.com",
    "vehicleType": "MOTORCYCLE",
    "vehicleNumber": "12가3456"
  }'
```

### 배송 생성
```bash
curl -X POST http://localhost:8080/api/deliveries \
  -H "Content-Type: application/json" \
  -d '{
    "orderNumber": "ORD-20251106-001",
    "pickupAddress": {
      "address": "서울특별시 강남구 테헤란로 123",
      "detailAddress": "ABC빌딩 1층",
      "latitude": 37.5012,
      "longitude": 127.0396,
      "contactName": "김상점",
      "contactPhone": "02-1234-5678"
    },
    "deliveryAddress": {
      "address": "서울특별시 강남구 역삼로 456",
      "detailAddress": "DEF아파트 101동 1001호",
      "latitude": 37.4979,
      "longitude": 127.0276,
      "contactName": "고객A",
      "contactPhone": "010-9876-5432"
    },
    "priority": "NORMAL",
    "itemDescription": "치킨 2마리 + 콜라 2병",
    "weight": 3.5,
    "quantity": 1,
    "deliveryFee": 3500
  }'
```

### 배송 추적
```bash
curl -X GET http://localhost:8080/api/deliveries/DEL-XYZ78901/track
```
