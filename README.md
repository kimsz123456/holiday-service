# holiday-service

## 빌드 & 실행 방법

---

## REST API 명세서

### 1. 데이터 초기 적재

#### `POST /api/holidays/init`

최초 실행 시 최근 5년(2021~2025)의 모든 국가 공휴일 데이터를 일괄 적재

**Request**

```http
POST /api/holidays/init
Content-Type: application/json
```

**Response**

```json
{
  "status": "SUCCESS",
  "message": "데이터 적재 완료",
  "data": {
    "totalCountries": 50,
    "totalYears": 6,
    "totalRecords": 3500,
    "processedAt": "2025-01-15T10:30:00"
  }
}
```

**Status Code**

- `200 OK`: 성공
- `500 Internal Server Error`: 외부 API 호출 실패

---

### 2. 공휴일 검색

#### `GET /api/holidays`

연도별, 국가별 필터 기반 공휴일 조회

**Request**

```http
GET /api/holidays?year=2025&countryCode=KR&dateFrom=2025-01-01&dateTo=2025-12-31&page=0&size=20&sort=asc
```

**Query Parameters**

| Parameter   | Type    | Required | Description                | Example    |
|-------------|---------|----------|----------------------------|------------|
| year        | Integer | No       | 조회 연도                      | 2025       |
| countryCode | String  | No       | 국가 코드 (ISO 3166-1 alpha-2) | KR, US     |
| dateFrom    | String  | No       | 시작 날짜 (yyyy-MM-dd)         | 2025-01-01 |
| dateTo      | String  | No       | 종료 날짜 (yyyy-MM-dd)         | 2025-12-31 |
| page        | Integer | No       | 페이지 번호 (0부터 시작, 기본값: 0)    | 0          |
| size        | Integer | No       | 페이지 크기 (기본값: 20, 최대: 100)  | 20         |
| sort        | String  | No       | 정렬 기준 (기본값: asc)           | asc / desc |

**Response (페이징)**

```json
{
  "content": [
    {
      "id": 1,
      "date": "2025-01-01",
      "localName": "신정",
      "name": "New Year's Day",
      "countryCode": "KR",
      "fixed": true,
      "global": true,
      "year": 2025
    },
    {
      "id": 2,
      "date": "2025-01-28",
      "localName": "설날",
      "name": "Korean New Year",
      "countryCode": "KR",
      "fixed": false,
      "global": true,
      "year": 2025
    }
  ],
  "totalElements": 15,
  "totalPages": 1,
  "number": 0,
  "size": 20,
  "first": true,
  "last": true,
  "empty": false
}
```

### 페이징 응답 구조 설명

- **content**: 실제 데이터 배열
- **totalElements**: 전체 데이터 개수
- **totalPages**: 전체 페이지 수
- **number**: 현재 페이지 번호 (0부터 시작)
- **size**: 페이지 크기
- **first**: 첫 페이지 여부
- **last**: 마지막 페이지 여부
- **empty**: 결과가 비어있는지 여부

**Status Code**

- `200 OK`: 성공
- `400 Bad Request`: 잘못된 파라미터

---

### 3. 데이터 재동기화

#### `POST /api/holidays/refresh`

특정 연도와 국가의 공휴일 데이터를 외부 API에서 다시 가져와 업데이트 (Upsert)

**Request**

```http
POST /api/holidays/refresh
Content-Type: application/json

{
  "year": 2025,
  "countryCode": "KR"
}
```

**Request Body**

| Field       | Type    | Required | Description | Example |
|-------------|---------|----------|-------------|---------|
| year        | Integer | Yes      | 재동기화할 연도    | 2025    |
| countryCode | String  | Yes      | 재동기화할 국가 코드 | KR      |

**Response**

```json
{
  "status": "SUCCESS",
  "message": "재동기화 완료",
  "data": {
    "year": 2025,
    "countryCode": "KR",
    "updatedRecords": 15,
    "insertedRecords": 2,
    "deletedRecords": 1,
    "processedAt": "2025-01-15T11:00:00"
  }
}
```

**Status Code**

- `200 OK`: 성공
- `400 Bad Request`: 필수 파라미터 누락
- `404 Not Found`: 유효하지 않은 국가 코드
- `500 Internal Server Error`: 외부 API 호출 실패

---

### 4. 데이터 삭제

#### `DELETE /api/holidays`

특정 연도와 국가의 모든 공휴일 데이터 삭제

**Request**

```http
DELETE /api/holidays?year=2025&countryCode=KR
```

**Query Parameters**

| Parameter   | Type    | Required | Description | Example |
|-------------|---------|----------|-------------|---------|
| year        | Integer | Yes      | 삭제할 연도      | 2025    |
| countryCode | String  | Yes      | 삭제할 국가 코드   | KR      |

**Response**

```json
{
  "status": "SUCCESS",
  "message": "삭제 완료",
  "data": {
    "year": 2025,
    "countryCode": "KR",
    "deletedRecords": 15,
    "processedAt": "2025-01-15T11:30:00"
  }
}
```

**Status Code**

- `200 OK`: 성공
- `400 Bad Request`: 필수 파라미터 누락
- `404 Not Found`: 삭제할 데이터가 없음

---

### 공통 에러 응답

```json
{
  "status": "ERROR",
  "message": "에러 메시지",
  "timestamp": "2025-01-15T11:45:00",
  "path": "/api/holidays/refresh"
}
```