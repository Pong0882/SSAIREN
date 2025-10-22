# SSAIREN Backend

## 기술 스택

- Java 21
- Spring Boot 3.5.6
- PostgreSQL 16
- Docker & Docker Compose

## 실행 방법

### Docker Compose 실행 (권장)

```bash
# 실행
docker-compose up -d

# 중지
docker-compose down

# DB 포함 완전 삭제
docker-compose down -v
```

### 로컬 실행

```bash
# DB만 Docker로 실행
docker-compose up -d postgres

# 애플리케이션 실행
./gradlew bootRun
```

## 접속 정보

- API: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui.html
- Health Check: http://localhost:8080/actuator/health

## 초기 데이터

dev 프로파일에서 자동으로 초기 데이터가 삽입됩니다:
- 소방서 25개
- 구급대원 테스트 데이터 4명

## 개발 가이드

### 코드 변경 후 재배포

```bash
docker-compose up -d --build backend
```

### DB 접속

```bash
docker-compose exec postgres psql -U {username} -d {database}
```

### 로그 확인

```bash
docker-compose logs -f backend
```

## 트러블슈팅

### DB 초기화

```bash
docker-compose down -v
docker-compose up -d
```

### 이미지 재빌드

```bash
docker-compose build --no-cache backend
docker-compose up -d
```