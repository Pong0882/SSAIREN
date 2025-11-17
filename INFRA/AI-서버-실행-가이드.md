# AI 서버 실행 가이드

## 목차
1. [프로젝트 구조](#프로젝트-구조)
2. [필수 파일 설정](#필수-파일-설정)
3. [Docker로 실행하기](#docker로-실행하기)
4. [nginx 설정 및 SSL 적용](#nginx-설정-및-ssl-적용)
5. [서비스 관리 명령어](#서비스-관리-명령어)
6. [트러블슈팅](#트러블슈팅)

---

## 프로젝트 구조

```
/home/ubuntu/ai/S13P31A205/AI/cloud/
├── app.py                    # FastAPI 애플리케이션
├── requirements.txt          # Python 의존성
├── Dockerfile               # Docker 이미지 빌드 설정
└── docker-compose.yml       # 컨테이너 실행 설정
```

---

## 필수 파일 설정

### 1. app.py (FastAPI 애플리케이션)

```python
from fastapi import FastAPI

app = FastAPI(title='CLOUD AI', description="클라우드 AI를 기반으로 한 서비스를 제공하는 서버")

@app.get("/")
def read_root():
    return {"message": "Hello, Cloud AI!"}
```

### 2. requirements.txt (Python 의존성)

```txt
fastapi==0.115.5
uvicorn[standard]==0.32.1
pydantic==2.10.3
```

### 3. Dockerfile

```dockerfile
FROM python:3.11-slim

WORKDIR /app

# 의존성 파일 복사 및 설치
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# 애플리케이션 코드 복사
COPY app.py .

# 포트 노출
EXPOSE 8000

# 헬스체크
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
    CMD python -c "import requests; requests.get('http://localhost:8000/')" || exit 1

# uvicorn으로 FastAPI 실행
CMD ["uvicorn", "app:app", "--host", "0.0.0.0", "--port", "8000"]
```

### 4. docker-compose.yml

```yaml
services:
  ai-cloud:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: ai-cloud
    restart: unless-stopped
    environment:
      - TZ=Asia/Seoul
    networks:
      - ssairen-net
    healthcheck:
      test: ["CMD-SHELL", "python -c \"import requests; requests.get('http://localhost:8000/')\" || exit 1"]
      interval: 30s
      timeout: 3s
      start_period: 10s
      retries: 3

networks:
  ssairen-net:
    external: true
```

---

## Docker로 실행하기

### 1단계: 프로젝트 디렉토리로 이동

```bash
cd /home/ubuntu/ai/S13P31A205/AI/cloud
```

### 2단계: Docker 컨테이너 빌드 및 실행

```bash
# 최초 실행 (빌드 포함)
sudo docker compose up -d --build

# 이미 빌드된 이미지로 실행
sudo docker compose up -d
```

### 3단계: 컨테이너 상태 확인

```bash
# 실행 중인 컨테이너 확인
sudo docker ps | grep ai-cloud

# 컨테이너 로그 확인
sudo docker logs ai-cloud

# 실시간 로그 확인
sudo docker logs -f ai-cloud
```

### 4단계: 헬스체크 확인

```bash
# 컨테이너 내부에서 API 테스트
sudo docker exec ai-cloud curl http://localhost:8000/

# 호스트에서 직접 테스트 (nginx 통해)
curl https://ai.ssairen.site/
```

---

## nginx 설정 및 SSL 적용

### 1단계: nginx 설정 파일 생성

**파일 위치**: `/home/ubuntu/nginx/conf.d/ai.conf`

```nginx
# AI 서버 설정 - HTTP (80 → 443 리다이렉트)
server {
    listen 80;
    listen [::]:80;
    server_name ai.ssairen.site;

    # Let's Encrypt 인증 경로
    location /.well-known/acme-challenge/ {
        root /var/www/certbot;
    }

    # 나머지 모든 요청은 HTTPS로 리다이렉트
    location / {
        return 301 https://$host$request_uri;
    }
}

# AI 서버 설정 - HTTPS (443)
server {
    listen 443 ssl;
    listen [::]:443 ssl;
    server_name ai.ssairen.site;

    # SSL 인증서 설정
    ssl_certificate /etc/letsencrypt/live/ai.ssairen.site/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/ai.ssairen.site/privkey.pem;

    # SSL 공통 설정 포함
    include /etc/nginx/includes/ssl-params.conf;

    # 로그 설정
    access_log /var/log/nginx/ai-access.log main;
    error_log /var/log/nginx/ai-error.log warn;

    # 프록시 설정 포함
    include /etc/nginx/includes/proxy-params.conf;

    # AI 서비스로 프록시
    location / {
        proxy_pass http://ai-cloud:8000;
    }
}
```

### 2단계: nginx 재시작

```bash
# nginx 설정 테스트
sudo docker exec nginx-server nginx -t

# nginx 재시작
cd /home/ubuntu
sudo docker compose restart nginx
```

### 3단계: SSL 인증서 발급

```bash
cd /home/ubuntu/certbot
./issue-cert.sh ai.ssairen.site
```

### 4단계: 최종 nginx 재시작

```bash
cd /home/ubuntu
sudo docker compose restart nginx
```

### 5단계: HTTPS 접속 테스트

```bash
# curl로 테스트
curl https://ai.ssairen.site/

# 브라우저로 접속
# https://ai.ssairen.site/
```

---

## 서비스 관리 명령어

### 기본 명령어

```bash
# 서비스 시작
cd /home/ubuntu/ai/S13P31A205/AI/cloud
sudo docker compose up -d

# 서비스 중지
sudo docker compose down

# 서비스 재시작
sudo docker compose restart

# 서비스 중지 및 컨테이너 삭제
sudo docker compose down --volumes
```

### 로그 확인

```bash
# 전체 로그 확인
sudo docker logs ai-cloud

# 최근 50줄만 확인
sudo docker logs ai-cloud --tail 50

# 실시간 로그 스트리밍
sudo docker logs -f ai-cloud

# nginx 로그 확인
sudo docker logs nginx-server --tail 50
```

### 컨테이너 내부 접근

```bash
# 컨테이너 셸 접속
sudo docker exec -it ai-cloud bash

# 컨테이너에서 명령 실행
sudo docker exec ai-cloud python --version
```

### 재빌드

```bash
# 코드 변경 후 재빌드 및 재시작
cd /home/ubuntu/ai/S13P31A205/AI/cloud
sudo docker compose down
sudo docker compose up -d --build
```

---

## 트러블슈팅

### 1. 컨테이너가 시작되지 않을 때

**증상**: `sudo docker ps`에서 ai-cloud가 보이지 않음

**해결 방법**:
```bash
# 모든 컨테이너 상태 확인 (중지된 것 포함)
sudo docker ps -a | grep ai-cloud

# 로그 확인
sudo docker logs ai-cloud

# 일반적인 원인:
# 1. 포트 충돌 (8000번 포트가 이미 사용 중)
# 2. 의존성 설치 실패
# 3. app.py 문법 오류
```

### 2. nginx에서 502 Bad Gateway 발생

**증상**: `https://ai.ssairen.site/` 접속 시 502 에러

**해결 방법**:
```bash
# ai-cloud 컨테이너 상태 확인
sudo docker ps | grep ai-cloud

# 헬스체크 상태 확인
sudo docker inspect ai-cloud | grep -A 10 Health

# ai-cloud가 실행 중이 아니면 시작
cd /home/ubuntu/ai/S13P31A205/AI/cloud
sudo docker compose up -d

# nginx와 ai-cloud가 같은 네트워크에 있는지 확인
sudo docker network inspect ssairen-net
```

### 3. SSL 인증서 갱신 필요

**증상**: 인증서 만료 경고

**해결 방법**:
```bash
# 수동 갱신
cd /home/ubuntu/certbot
./renew-certs.sh

# 자동 갱신 설정 (crontab)
crontab -e
# 추가: 0 0 * * * /home/ubuntu/certbot/renew-certs.sh
```

### 4. 의존성 추가/변경 시

**증상**: requirements.txt를 수정했지만 반영되지 않음

**해결 방법**:
```bash
# 컨테이너를 완전히 제거하고 재빌드
cd /home/ubuntu/ai/S13P31A205/AI/cloud
sudo docker compose down
sudo docker compose up -d --build --force-recreate
```

### 5. 네트워크 연결 문제

**증상**: nginx에서 ai-cloud를 찾을 수 없음

**해결 방법**:
```bash
# ssairen-net 네트워크 확인
sudo docker network inspect ssairen-net

# 네트워크가 없으면 생성
sudo docker network create ssairen-net

# 컨테이너 재시작
cd /home/ubuntu/ai/S13P31A205/AI/cloud
sudo docker compose down
sudo docker compose up -d
```

---

## 빠른 참조

### 포트 정보
- **내부 포트**: 8000 (FastAPI 기본 포트)
- **외부 포트**: 443 (HTTPS)
- **도메인**: ai.ssairen.site

### 주요 경로
- **프로젝트**: `/home/ubuntu/ai/S13P31A205/AI/cloud/`
- **nginx 설정**: `/home/ubuntu/nginx/conf.d/ai.conf`
- **SSL 인증서**: `/home/ubuntu/certbot-data/conf/live/ai.ssairen.site/`
- **로그**: `sudo docker logs ai-cloud`

### 상태 확인 체크리스트

```bash
# 1. AI 컨테이너 실행 확인
sudo docker ps | grep ai-cloud

# 2. 헬스체크 상태 확인
sudo docker inspect ai-cloud | grep -A 5 Health

# 3. 네트워크 연결 확인
sudo docker network inspect ssairen-net | grep ai-cloud

# 4. nginx 설정 확인
sudo docker exec nginx-server nginx -t

# 5. API 응답 테스트
curl https://ai.ssairen.site/

# 6. SSL 인증서 확인
sudo docker compose -f /home/ubuntu/certbot/docker-compose.yml run --rm certbot certificates
```

---

## 개발 워크플로우

### 코드 수정 후 배포

1. **코드 수정**
   ```bash
   nano /home/ubuntu/ai/S13P31A205/AI/cloud/app.py
   ```

2. **컨테이너 재빌드**
   ```bash
   cd /home/ubuntu/ai/S13P31A205/AI/cloud
   sudo docker compose down
   sudo docker compose up -d --build
   ```

3. **테스트**
   ```bash
   curl https://ai.ssairen.site/
   ```

### 새로운 엔드포인트 추가

1. **app.py 수정**
   ```python
   @app.get("/health")
   def health_check():
       return {"status": "healthy"}
   ```

2. **재빌드 및 재시작**
   ```bash
   cd /home/ubuntu/ai/S13P31A205/AI/cloud
   sudo docker compose up -d --build
   ```

3. **테스트**
   ```bash
   curl https://ai.ssairen.site/health
   ```

---

**작성일**: 2025-10-27
**버전**: 1.0
**서비스**: SSAIREN AI Cloud Service
