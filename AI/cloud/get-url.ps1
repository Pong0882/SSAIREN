# ngrok 공개 URL 확인 스크립트

Write-Host "ngrok URL 확인 중..." -ForegroundColor Green

try {
    $response = Invoke-RestMethod -Uri "http://localhost:4040/api/tunnels"
    $url = $response.tunnels[0].public_url

    if ($url) {
        Write-Host "`n공개 URL:" -ForegroundColor Yellow
        Write-Host $url -ForegroundColor Cyan
        Write-Host "`n웹 인터페이스: http://localhost:4040" -ForegroundColor Gray
    } else {
        Write-Host "URL을 찾을 수 없습니다. ngrok이 실행 중인지 확인하세요." -ForegroundColor Red
    }
} catch {
    Write-Host "오류: ngrok이 실행 중이지 않습니다." -ForegroundColor Red
    Write-Host "다음 명령으로 실행하세요: docker-compose up -d ngrok" -ForegroundColor Yellow
}
