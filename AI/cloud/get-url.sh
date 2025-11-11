#!/bin/bash
# ngrok 공개 URL 확인 스크립트

echo -e "\033[32mngrok URL 확인 중...\033[0m"

URL=$(curl -s http://localhost:4040/api/tunnels | grep -o '"public_url":"https://[^"]*' | cut -d'"' -f4 | head -n1)

if [ -n "$URL" ]; then
    echo -e "\n\033[33m공개 URL:\033[0m"
    echo -e "\033[36m$URL\033[0m"
    echo -e "\n\033[90m웹 인터페이스: http://localhost:4040\033[0m"
else
    echo -e "\033[31m오류: ngrok이 실행 중이지 않습니다.\033[0m"
    echo -e "\033[33m다음 명령으로 실행하세요: docker-compose up -d ngrok\033[0m"
fi
