# SSL 인증서 발급 가이드

## 목차
1. [새로운 도메인에 SSL 발급하기](#새로운-도메인에-ssl-발급하기)
2. [여러 도메인 동시 발급](#여러-도메인-동시-발급)
3. [SSL 인증서 갱신](#ssl-인증서-갱신)
4. [트러블슈팅](#트러블슈팅)

---

## 새로운 도메인에 SSL 발급하기

### 1단계: nginx 설정 파일 생성

먼저 HTTP (80포트) 설정만 있는 nginx 설정 파일을 생성합니다.

**예시: be.ssairen.site**

```bash
# nginx 설정 파일 생성
sudo nano /home/ubuntu/nginx/conf.d/backend.conf
```

```nginx
# Backend 서버 설정 - HTTP (임시, SSL 인증서 발급용)
server {
    listen 80;
    listen [::]:80;
    server_name be.ssairen.site;

    # 로그 설정
    access_log /var/log/nginx/backend-access.log main;
    error_log /var/log/nginx/backend-error.log warn;

    # Let's Encrypt 인증 경로 (필수!)
    location /.well-known/acme-challenge/ {
        root /var/www/certbot;
    }

    # 백엔드로 프록시
    location / {
        proxy_pass http://backend:8080;
        include /etc/nginx/includes/proxy-params.conf;
    }
}
```

### 2단계: nginx 재시작

```bash
cd /home/ubuntu
sudo docker compose restart nginx

# 설정 테스트
sudo docker exec nginx-server nginx -t
```

### 3단계: SSL 인증서 발급

```bash
cd /home/ubuntu/certbot
./issue-cert.sh be.ssairen.site
```

### 4단계: nginx에 SSL 설정 추가

기존 설정 파일을 SSL이 적용된 버전으로 교체합니다.

```bash
sudo nano /home/ubuntu/nginx/conf.d/backend.conf
```

```nginx
# Backend 서버 설정 - HTTP (80 → 443 리다이렉트)
server {
    listen 80;
    listen [::]:80;
    server_name be.ssairen.site;

    # Let's Encrypt 인증 경로
    location /.well-known/acme-challenge/ {
        root /var/www/certbot;
    }

    # HTTPS로 리다이렉트
    location / {
        return 301 https://$host$request_uri;
    }
}

# Backend 서버 설정 - HTTPS (443)
server {
    listen 443 ssl;
    listen [::]:443 ssl;
    server_name be.ssairen.site;

    # SSL 인증서 설정
    ssl_certificate /etc/letsencrypt/live/be.ssairen.site/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/be.ssairen.site/privkey.pem;

    # SSL 공통 설정 포함
    include /etc/nginx/includes/ssl-params.conf;

    # 로그 설정
    access_log /var/log/nginx/backend-access.log main;
    error_log /var/log/nginx/backend-error.log warn;

    # 백엔드로 프록시
    location / {
        proxy_pass http://backend:8080;
        include /etc/nginx/includes/proxy-params.conf;
    }
}
```

### 5단계: nginx 재시작 및 확인

```bash
# 설정 테스트
sudo docker exec nginx-server nginx -t

# nginx 재시작
cd /home/ubuntu
sudo docker compose restart nginx

# 로그 확인
sudo docker logs nginx-server --tail 20
```

### 6단계: 브라우저에서 테스트

- `http://be.ssairen.site` 접속
- 자동으로 `https://be.ssairen.site`로 리다이렉트되는지 확인
- 브라우저 주소창에 자물쇠 아이콘이 표시되는지 확인

---

## 여러 도메인 동시 발급

하나의 인증서에 여러 도메인을 포함시킬 수 있습니다.

### 예시: ssairen.site + www.ssairen.site

```bash
cd /home/ubuntu/certbot

# 스크립트 직접 수정하거나 docker compose 명령 사용
sudo docker compose run --rm certbot certonly \
    --webroot \
    --webroot-path=/var/www/certbot \
    --email parkdanilssafy@gmail.com \
    --agree-tos \
    --no-eff-email \
    -d ssairen.site \
    -d www.ssairen.site
```

nginx 설정에서는:

```nginx
server {
    listen 443 ssl;
    server_name ssairen.site www.ssairen.site;

    ssl_certificate /etc/letsencrypt/live/ssairen.site/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/ssairen.site/privkey.pem;

    # ... 나머지 설정
}
```

---

## SSL 인증서 갱신

Let's Encrypt 인증서는 **90일**마다 갱신이 필요합니다.

### 수동 갱신

```bash
cd /home/ubuntu/certbot
./renew-certs.sh
```

### 자동 갱신 설정 (cron)

```bash
# crontab 편집
crontab -e

# 아래 내용 추가: 매일 자정에 갱신 시도
0 0 * * * /home/ubuntu/certbot/renew-certs.sh >> /var/log/certbot-renew.log 2>&1
```

### 갱신 상태 확인

```bash
sudo docker compose -f /home/ubuntu/certbot/docker-compose.yml run --rm certbot certificates
```

---

## 트러블슈팅

### 1. 인증서 발급 실패: "Timeout during connect"

**원인:** 도메인이 서버 IP로 제대로 연결되지 않음

**해결:**
```bash
# DNS 확인
nslookup jenkins.ssairen.site

# 80포트 열려있는지 확인
sudo docker ps | grep nginx
```

### 2. nginx 시작 실패: "cannot load certificate"

**원인:** SSL 인증서가 아직 발급되지 않았는데 SSL 설정이 활성화됨

**해결:**
```bash
# 1. SSL 설정 비활성화
sudo mv /home/ubuntu/nginx/conf.d/backend.conf /home/ubuntu/nginx/conf.d/backend.conf.ssl

# 2. HTTP만 있는 임시 설정 파일 생성
sudo nano /home/ubuntu/nginx/conf.d/backend.conf
# (1단계 설정 사용)

# 3. nginx 재시작
sudo docker compose restart nginx

# 4. SSL 발급 후 다시 활성화
```

### 3. "ssl_stapling" 경고 메시지

**경고 메시지:**
```
nginx: [warn] "ssl_stapling" ignored, no OCSP responder URL in the certificate
```

**해결:** 이 경고는 무시해도 됩니다. Let's Encrypt 인증서 특성상 나타나는 메시지입니다.

### 4. 인증서 갱신 실패

**원인:** nginx가 /.well-known/acme-challenge/ 경로를 제대로 처리하지 못함

**해결:**
```bash
# nginx 설정에 반드시 포함되어 있는지 확인
location /.well-known/acme-challenge/ {
    root /var/www/certbot;
}
```

---

## 빠른 참조

### 디렉토리 구조

```
/home/ubuntu/
├── docker-compose.yml          # nginx 설정
├── certbot/
│   ├── docker-compose.yml      # certbot 설정
│   ├── issue-cert.sh          # SSL 발급 스크립트
│   └── renew-certs.sh         # SSL 갱신 스크립트
├── certbot-data/
│   ├── conf/                  # SSL 인증서 저장
│   ├── www/                   # certbot 검증 파일
│   └── logs/                  # certbot 로그
└── nginx/
    ├── nginx.conf             # nginx 메인 설정
    ├── conf.d/                # 도메인별 설정
    │   ├── jenkins.conf
    │   ├── backend.conf
    │   └── frontend.conf
    └── includes/              # 공통 설정
        ├── proxy-params.conf
        └── ssl-params.conf
```

### 주요 명령어

```bash
# SSL 인증서 발급
cd /home/ubuntu/certbot && ./issue-cert.sh <도메인>

# SSL 인증서 갱신
cd /home/ubuntu/certbot && ./renew-certs.sh

# nginx 설정 테스트
sudo docker exec nginx-server nginx -t

# nginx 재시작
cd /home/ubuntu && sudo docker compose restart nginx

# nginx 로그 확인
sudo docker logs nginx-server --tail 50

# 인증서 목록 확인
sudo docker compose -f /home/ubuntu/certbot/docker-compose.yml run --rm certbot certificates
```

---

## 체크리스트

새로운 도메인에 SSL을 적용할 때 이 체크리스트를 따라가세요:

- [ ] 1. DNS A 레코드가 서버 IP를 가리키는지 확인
- [ ] 2. nginx HTTP (80) 설정 파일 생성
- [ ] 3. `location /.well-known/acme-challenge/` 경로 포함 확인
- [ ] 4. nginx 재시작 및 설정 테스트
- [ ] 5. `./issue-cert.sh <도메인>` 실행
- [ ] 6. SSL 인증서 발급 성공 확인
- [ ] 7. nginx 설정에 HTTPS (443) 설정 추가
- [ ] 8. HTTP → HTTPS 리다이렉트 추가
- [ ] 9. nginx 재시작
- [ ] 10. 브라우저에서 HTTPS 접속 테스트

---

**참고:**
- 이메일: parkdanilssafy@gmail.com
- 인증서 유효기간: 90일
- 자동 갱신 권장
