#!/bin/bash

# SSL 인증서 발급 스크립트
# 사용법: ./issue-cert.sh <도메인>

DOMAIN=$1
EMAIL="parkdanilssafy@gmail.com"

if [ -z "$DOMAIN" ]; then
    echo "사용법: ./issue-cert.sh <도메인>"
    echo "예시: ./issue-cert.sh jenkins.ssairen.site"
    exit 1
fi

echo "======================================"
echo "SSL 인증서 발급 시작"
echo "======================================"
echo "도메인: $DOMAIN"
echo "이메일: $EMAIL"
echo ""

# certbot으로 SSL 인증서 발급
sudo docker compose run --rm certbot certonly \
    --webroot \
    --webroot-path=/var/www/certbot \
    --email $EMAIL \
    --agree-tos \
    --no-eff-email \
    -d $DOMAIN

if [ $? -eq 0 ]; then
    echo ""
    echo "======================================"
    echo "✓ SSL 인증서 발급 완료!"
    echo "======================================"
    echo ""
    echo "다음 단계:"
    echo "1. nginx 설정 파일에서 SSL 설정을 활성화하세요"
    echo "2. nginx를 재시작하세요:"
    echo "   cd /home/ubuntu && sudo docker compose restart nginx"
else
    echo ""
    echo "======================================"
    echo "✗ SSL 인증서 발급 실패"
    echo "======================================"
    exit 1
fi
