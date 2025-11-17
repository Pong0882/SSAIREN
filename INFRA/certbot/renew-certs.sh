#!/bin/bash

# SSL 인증서 갱신 스크립트
# cron으로 자동 실행하거나 수동으로 실행 가능

echo "======================================"
echo "SSL 인증서 갱신 시작"
echo "======================================"

# certbot으로 인증서 갱신
docker compose run --rm certbot renew

if [ $? -eq 0 ]; then
    echo ""
    echo "======================================"
    echo "✓ SSL 인증서 갱신 완료!"
    echo "======================================"
    echo ""
    echo "nginx 재시작 중..."
    cd /home/ubuntu && sudo docker compose restart nginx
    echo "✓ nginx 재시작 완료"
else
    echo ""
    echo "======================================"
    echo "✗ SSL 인증서 갱신 실패 또는 갱신 불필요"
    echo "======================================"
fi
