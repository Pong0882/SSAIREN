# SSAIREN 포팅 매뉴얼

## 📋 목차
1. [서버 환경](#서버-환경)
2. [사전 요구사항](#사전-요구사항)
3. [인프라 구조](#인프라-구조)
4. [서비스별 배포 가이드](#서비스별-배포-가이드)
5. [환경변수 설정](#환경변수-설정)
6. [배포 프로세스](#배포-프로세스)
7. [트러블슈팅](#트러블슈팅)

---

## 서버 환경

### 운영 서버 사양
- **OS**: Ubuntu 22.04 LTS (Linux 6.8.0-1040-aws)
- **클라우드**: AWS EC2
- **도메인**: ssairen.site
- **SSL**: Let's Encrypt (자동 갱신)

### 설치된 소프트웨어
- Docker 27.x
- Docker Compose 2.x
- Nginx (Docker 컨테이너)
- Jenkins (Docker 컨테이너)

---

## 사전 요구사항

### 1. Docker 설치
```bash
# Docker 설치
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# 사용자를 docker 그룹에 추가
sudo usermod -aG docker $USER

# Docker Compose 설치 확인
docker compose version
```

### 2. 필수 디렉토리 구조
```
/home/ubuntu/
├── backend/S13P31A205/
│   ├── BE/SSAIREN/          # 백엔드 소스코드
│   └── FE/                  # 프론트엔드 소스코드
├── nginx/
│   ├── nginx.conf           # Nginx 메인 설정
│   ├── conf.d/              # 서비스별 설정
│   ├── includes/            # 공통 설정 (ssl-params, proxy-params)
│   └── logs/                # Nginx 로그
└── docker-volumes/          # Docker 영구 볼륨
    ├── jenkins_home/
    ├── prometheus/
    ├── grafana/
    └── loki/
```

### 3. Docker 네트워크 생성
```bash
docker network create ssairen-net
```

---

## 인프라 구조

### 전체 아키텍처
```
                          ┌─────────────────┐
                          │   CloudFlare    │
                          │   (DNS & CDN)   │
                          └────────┬────────┘
                                   │
                          ┌────────▼────────┐
                          │  AWS EC2 Server │
                          │  (Ubuntu 22.04) │
                          └────────┬────────┘
                                   │
                   ┌───────────────┼───────────────┐
                   │               │               │
          ┌────────▼────────┐  ┌──▼──────┐  ┌────▼─────┐
          │  Nginx (80/443) │  │ Jenkins │  │   MinIO  │
          │   Reverse Proxy │  │ (CI/CD) │  │ (Storage)│
          └────────┬────────┘  └─────────┘  └──────────┘
                   │
      ┌────────────┼────────────┐
      │            │            │
┌─────▼─────┐ ┌───▼────┐ ┌────▼──────┐
│  Backend  │ │   AI   │ │ Frontend  │
│ (Blue/Grn)│ │ Server │ │  (React)  │
└─────┬─────┘ └────────┘ └───────────┘
      │
┌─────▼──────┬──────────┬──────────┐
│ PostgreSQL │  Redis   │  MinIO   │
│   (DB)     │ (Cache)  │ (Files)  │
└────────────┴──────────┴──────────┘
```

### Docker 컨테이너 목록

| 컨테이너명 | 이미지 | 포트 | 용도 |
|-----------|--------|------|------|
| nginx-server | nginx:latest | 80, 443 | 리버스 프록시, SSL 종단 |
| jenkins | jenkins/jenkins:lts | 8080, 50000 | CI/CD 파이프라인 |
| backend | ssairen-backend | 18080 | 백엔드 API (기존, 안정성) |
| backend-blue | ssairen-backend-blue | - | 블루-그린 배포 (Blue) |
| backend-green | ssairen-backend-green | - | 블루-그린 배포 (Green) |
| ssairen-frontend | ssairen-frontend | - | React 프론트엔드 |
| ai-cloud | cloud-ai-cloud | 8000 | AI 서버 (FastAPI) |
| ssairen-postgres | postgres:16-alpine | 5432 | PostgreSQL 데이터베이스 |
| ssairen-redis | redis:7-alpine | 6379 | Redis 캐시 |
| ssairen-redisinsight | redis/redisinsight | 5540 | Redis 모니터링 |
| minio | minio/minio | 9000 | 오브젝트 스토리지 |
| prometheus | prom/prometheus | 9090 | 메트릭 수집 |
| grafana | grafana/grafana | 3000 | 모니터링 대시보드 |
| loki | grafana/loki | 3100 | 로그 집계 |
| promtail | grafana/promtail | - | 로그 수집 |
| cadvisor | gcr.io/cadvisor/cadvisor | 8080 | 컨테이너 메트릭 |
| node-exporter | prom/node-exporter | 9100 | 노드 메트릭 |

### 도메인 매핑

| 도메인 | 서비스 | 설명 |
|--------|--------|------|
| ssairen.site | Frontend | React 프론트엔드 |
| api.ssairen.site | Backend (기존) | Spring Boot API (안정성 보장) |
| be.ssairen.site | Backend (Blue/Green) | 블루-그린 무중단 배포 |
| ai.ssairen.site | AI Server | FastAPI AI 서버 |
| jenkins.ssairen.site | Jenkins | CI/CD 대시보드 |
| minio.ssairen.site | MinIO Console | 스토리지 관리 |
| minio-api.ssairen.site | MinIO API | S3 호환 API |
| grafana.ssairen.site | Grafana | 모니터링 대시보드 |
| prometheus.ssairen.site | Prometheus | 메트릭 수집 |
| loki.ssairen.site | Loki | 로그 조회 |
| cadvisor.ssairen.site | cAdvisor | 컨테이너 모니터링 |
| redis.ssairen.site | RedisInsight | Redis 관리 |

---

## 서비스별 배포 가이드

### 1. Nginx (리버스 프록시)

#### 설치 및 실행
```bash
# Nginx 디렉토리 생성
mkdir -p ~/nginx/{conf.d,includes,logs}

# Nginx 실행
docker run -d \
  --name nginx-server \
  --network ssairen-net \
  -p 80:80 \
  -p 443:443 \
  -v ~/nginx/nginx.conf:/etc/nginx/nginx.conf:ro \
  -v ~/nginx/conf.d:/etc/nginx/conf.d:ro \
  -v ~/nginx/includes:/etc/nginx/includes:ro \
  -v ~/nginx/logs:/var/log/nginx \
  -v /etc/letsencrypt:/etc/letsencrypt:ro \
  --restart unless-stopped \
  nginx:latest
```

#### SSL 인증서 발급 (Let's Encrypt)
```bash
# Certbot 설치
sudo apt install certbot python3-certbot-nginx

# 인증서 발급
sudo certbot certonly --standalone -d ssairen.site -d www.ssairen.site
sudo certbot certonly --standalone -d api.ssairen.site
sudo certbot certonly --standalone -d be.ssairen.site
# ... (기타 서브도메인)

# 자동 갱신 설정
sudo crontab -e
# 추가: 0 3 * * * certbot renew --quiet
```

#### Nginx 설정 구조
```bash
/home/ubuntu/nginx/
├── nginx.conf                    # 메인 설정
├── conf.d/
│   ├── backend.conf              # api.ssairen.site (기존)
│   ├── backend-blue.conf.template   # Blue 설정
│   ├── backend-green.conf.template  # Green 설정
│   ├── backend-be.conf           # be.ssairen.site (symlink)
│   ├── frontend.conf             # ssairen.site
│   ├── ai.conf                   # ai.ssairen.site
│   ├── jenkins.conf              # jenkins.ssairen.site
│   ├── minio.conf                # minio.ssairen.site
│   ├── minio-api.conf            # minio-api.ssairen.site
│   ├── grafana.conf              # grafana.ssairen.site
│   ├── prometheus.conf           # prometheus.ssairen.site
│   ├── loki.conf                 # loki.ssairen.site
│   ├── cadvisor.conf             # cadvisor.ssairen.site
│   └── redis.conf                # redis.ssairen.site
└── includes/
    ├── ssl-params.conf           # SSL 공통 설정
    └── proxy-params.conf         # Proxy 공통 설정
```

---

### 2. Backend (Spring Boot)

#### 기본 설정
```bash
cd ~/backend/S13P31A205/BE/SSAIREN
```

#### 환경변수 파일 (.env)
```env
# PostgreSQL
POSTGRES_DB=ssairen_db
POSTGRES_USER=your_username
POSTGRES_PASSWORD=your_password
POSTGRES_PORT=5432

# Redis
REDIS_PASSWORD=your_redis_password
REDIS_PORT=6379

# MinIO
MINIO_ENDPOINT=https://minio-api.ssairen.site
MINIO_ACCESS_KEY=your_access_key
MINIO_SECRET_KEY=your_secret_key
MINIO_BUCKET_NAME=audio-files

# JWT
JWT_SECRET=your_jwt_secret_key
JWT_ACCESS_TOKEN_EXPIRATION=3600000
JWT_REFRESH_TOKEN_EXPIRATION=86400000

# Firebase
FIREBASE_CREDENTIALS={"type":"service_account",...}
```

#### 일반 배포 (기존 방식)
```bash
# docker-compose.yml 사용
docker compose up -d backend
```

#### 블루-그린 무중단 배포

**초기 설정:**
```bash
# 1. Active color 파일 생성 (Jenkins 컨테이너 내부)
docker exec jenkins sh -c 'echo "blue" > /var/jenkins_home/active_color'

# 2. Nginx symlink 초기 설정
ln -sf /home/ubuntu/nginx/conf.d/backend-blue.conf.template \
       /home/ubuntu/nginx/conf.d/backend-be.conf
```

**Jenkins 파이프라인 설정:**
1. Jenkins 웹 UI 접속 (`https://jenkins.ssairen.site`)
2. New Item → `Backend-BlueGreen-Deploy` (Pipeline)
3. Pipeline 설정:
   - SCM: Git
   - Repository: `https://lab.ssafy.com/s13-final/S13P31A205.git`
   - Branch: `*/BE/feature/blue-green-deployment`
   - Script Path: `BE/SSAIREN/Jenkinsfile.BlueGreen`

**배포 실행:**
```bash
# Jenkins에서 "Build Now" 클릭
# 또는 Git push 시 자동 트리거
```

**배포 프로세스:**
1. 현재 활성 색상 확인 (blue/green)
2. 타깃 색상 결정 (반대 색상)
3. 타깃 컨테이너 빌드 & 배포
4. 헬스체크 (`/actuator/health/liveness`)
5. Nginx symlink 전환
6. Active color 파일 업데이트

**수동 롤백 (필요시):**
```bash
# Green → Blue로 롤백
docker exec nginx-server sh -c \
  'ln -sf /etc/nginx/conf.d/backend-blue.conf.template /etc/nginx/conf.d/backend-be.conf && nginx -s reload'

echo "blue" | docker exec -i jenkins sh -c 'cat > /var/jenkins_home/active_color'
```

---

### 3. Frontend (React)

#### Dockerfile
```dockerfile
FROM node:18-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

#### 배포
```bash
cd ~/backend/S13P31A205/FE
docker build -t ssairen-frontend .
docker run -d \
  --name ssairen-frontend \
  --network ssairen-net \
  --restart unless-stopped \
  ssairen-frontend
```

---

### 4. AI Server (FastAPI)

#### 실행
```bash
docker run -d \
  --name ai-cloud \
  --network ssairen-net \
  -p 8000:8000 \
  --restart unless-stopped \
  cloud-ai-cloud
```

---

### 5. PostgreSQL

#### 실행
```bash
docker run -d \
  --name ssairen-postgres \
  --network ssairen-net \
  -p 5432:5432 \
  -e POSTGRES_DB=ssairen_db \
  -e POSTGRES_USER=your_username \
  -e POSTGRES_PASSWORD=your_password \
  -v postgres_data:/var/lib/postgresql/data \
  --restart unless-stopped \
  postgres:16-alpine
```

#### 백업
```bash
# 데이터베이스 백업
docker exec ssairen-postgres pg_dump -U your_username ssairen_db > backup.sql

# 복원
docker exec -i ssairen-postgres psql -U your_username ssairen_db < backup.sql
```

---

### 6. Redis

#### 실행
```bash
docker run -d \
  --name ssairen-redis \
  --network ssairen-net \
  -p 6379:6379 \
  --restart unless-stopped \
  redis:7-alpine redis-server --requirepass your_redis_password
```

---

### 7. MinIO (Object Storage)

#### 실행
```bash
docker run -d \
  --name minio \
  --network ssairen-net \
  -p 9000:9000 \
  -e MINIO_ROOT_USER=admin \
  -e MINIO_ROOT_PASSWORD=your_password \
  -v minio_data:/data \
  --restart unless-stopped \
  minio/minio server /data --console-address ":9001"
```

#### 버킷 생성
```bash
# MinIO 콘솔 접속 (https://minio.ssairen.site)
# 버킷 생성: audio-files, video-files
```

---

### 8. Jenkins (CI/CD)

#### 실행
```bash
docker run -d \
  --name jenkins \
  --network ssairen-net \
  -p 8080:8080 \
  -p 50000:50000 \
  -v jenkins_home:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  --restart unless-stopped \
  jenkins/jenkins:lts

# 초기 비밀번호 확인
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

#### 필수 플러그인
- Git
- Pipeline
- Docker Pipeline
- GitLab (GitLab 연동 시)

#### Credentials 설정
1. Jenkins 관리 → Credentials
2. Global credentials 추가:
   - GitLab Token
   - Backend `.env` file

---

### 9. 모니터링 스택 (Prometheus + Grafana + Loki)

#### Prometheus
```bash
docker run -d \
  --name prometheus \
  --network ssairen-net \
  -p 9090:9090 \
  -v ~/prometheus/prometheus.yml:/etc/prometheus/prometheus.yml \
  --restart unless-stopped \
  prom/prometheus
```

#### Grafana
```bash
docker run -d \
  --name grafana \
  --network ssairen-net \
  -p 3000:3000 \
  -e GF_SECURITY_ADMIN_PASSWORD=your_password \
  -v grafana_data:/var/lib/grafana \
  --restart unless-stopped \
  grafana/grafana
```

#### Loki
```bash
docker run -d \
  --name loki \
  --network ssairen-net \
  -p 3100:3100 \
  -v ~/loki/loki-config.yml:/etc/loki/local-config.yaml \
  --restart unless-stopped \
  grafana/loki
```

---

## 환경변수 설정

### Backend (.env)
Jenkins Credentials에 등록:
- Credential ID: `backend-env-file`
- Type: Secret file
- File: `.env` (위 "Backend 환경변수" 참조)

### 환경변수 우선순위
1. Jenkins에서 주입되는 `.env` 파일
2. Docker Compose의 environment 섹션
3. Spring Boot의 application.yml

---

## 배포 프로세스

### 1. 백엔드 배포 (블루-그린)
```bash
# 1. 코드 수정 후 Git push
git add .
git commit -m "feat: 새 기능 추가"
git push origin BE/feature/blue-green-deployment

# 2. Jenkins에서 자동 빌드 트리거
# 또는 수동으로 "Build Now" 클릭

# 3. 배포 프로세스
# - Git Checkout
# - 색상 결정 (active → target)
# - Docker 이미지 빌드
# - 타깃 컨테이너 배포
# - 헬스체크
# - Nginx 전환
# - Active color 업데이트

# 4. 배포 확인
curl https://be.ssairen.site/actuator/health
```

### 2. 프론트엔드 배포
```bash
cd ~/backend/S13P31A205/FE

# 빌드
docker build -t ssairen-frontend .

# 기존 컨테이너 중지 & 제거
docker stop ssairen-frontend
docker rm ssairen-frontend

# 새 컨테이너 실행
docker run -d \
  --name ssairen-frontend \
  --network ssairen-net \
  --restart unless-stopped \
  ssairen-frontend

# Nginx reload
docker exec nginx-server nginx -s reload
```

### 3. AI 서버 배포
```bash
# 이미지 빌드
docker build -t cloud-ai-cloud .

# 컨테이너 재시작
docker stop ai-cloud
docker rm ai-cloud
docker run -d \
  --name ai-cloud \
  --network ssairen-net \
  -p 8000:8000 \
  --restart unless-stopped \
  cloud-ai-cloud
```

---

## 트러블슈팅

### 1. 컨테이너가 시작되지 않을 때
```bash
# 로그 확인
docker logs <container_name> --tail 100

# 컨테이너 상태 확인
docker ps -a

# 네트워크 확인
docker network inspect ssairen-net
```

### 2. Nginx 설정 오류
```bash
# 설정 테스트
docker exec nginx-server nginx -t

# 설정 reload
docker exec nginx-server nginx -s reload

# Nginx 로그 확인
tail -f ~/nginx/logs/error.log
```

### 3. 블루-그린 배포 실패
```bash
# 현재 활성 색상 확인
docker exec jenkins cat /var/jenkins_home/active_color

# Symlink 확인
ls -la ~/nginx/conf.d/backend-be.conf

# 수동 롤백
docker exec nginx-server sh -c \
  'ln -sf /etc/nginx/conf.d/backend-blue.conf.template /etc/nginx/conf.d/backend-be.conf && nginx -s reload'
```

### 4. 데이터베이스 연결 오류
```bash
# PostgreSQL 연결 테스트
docker exec -it ssairen-postgres psql -U your_username -d ssairen_db

# 컨테이너 재시작
docker restart ssairen-postgres

# 네트워크 연결 확인
docker exec backend ping ssairen-postgres
```

### 5. SSL 인증서 갱신 실패
```bash
# 수동 갱신
sudo certbot renew --force-renewal

# Nginx reload
docker exec nginx-server nginx -s reload
```

---

## 유용한 명령어

### Docker 관리
```bash
# 모든 컨테이너 상태 확인
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

# 디스크 사용량 확인
docker system df

# 사용하지 않는 리소스 정리
docker system prune -a

# 로그 확인
docker logs <container> --tail 100 -f

# 컨테이너 재시작
docker restart <container>
```

### Nginx 관리
```bash
# 설정 테스트
docker exec nginx-server nginx -t

# Reload
docker exec nginx-server nginx -s reload

# 로그 확인
tail -f ~/nginx/logs/access.log
tail -f ~/nginx/logs/error.log
```

### 데이터베이스 관리
```bash
# PostgreSQL 백업
docker exec ssairen-postgres pg_dump -U username dbname > backup_$(date +%Y%m%d).sql

# Redis CLI 접속
docker exec -it ssairen-redis redis-cli -a your_password
```

---

## 보안 권장사항

1. **방화벽 설정**
   - 필요한 포트만 개방 (80, 443, 22)
   - 관리 포트는 내부 네트워크에서만 접근

2. **환경변수 관리**
   - `.env` 파일은 Git에 커밋하지 않음
   - Jenkins Credentials로 안전하게 관리

3. **SSL/TLS**
   - 모든 서비스 HTTPS 사용
   - Let's Encrypt 자동 갱신 설정

4. **정기 업데이트**
   - Docker 이미지 정기 업데이트
   - 보안 패치 적용

5. **백업**
   - 데이터베이스 일일 백업
   - Docker 볼륨 정기 백업

---

## 참고 자료

- Docker 공식 문서: https://docs.docker.com
- Nginx 문서: https://nginx.org/en/docs/
- Spring Boot 문서: https://spring.io/projects/spring-boot
- Let's Encrypt: https://letsencrypt.org

---

**작성일**: 2025-11-17
**버전**: 1.0
**담당**: SSAIREN 개발팀
